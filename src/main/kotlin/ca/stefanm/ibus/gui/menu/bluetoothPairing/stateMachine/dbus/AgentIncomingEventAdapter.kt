package ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.dbus

import ca.stefanm.ibus.car.platform.Service
import ca.stefanm.ibus.gui.menu.bluetoothPairing.stateMachine.*
import ca.stefanm.ibus.lib.logging.Logger
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.bluez.Agent1
import org.bluez.AgentManager1
import org.bluez.exceptions.BluezCanceledException
import org.bluez.exceptions.BluezDoesNotExistException
import org.bluez.exceptions.BluezRejectedException
import org.freedesktop.dbus.DBusPath
import org.freedesktop.dbus.connections.impl.DBusConnection
import org.freedesktop.dbus.types.UInt16
import org.freedesktop.dbus.types.UInt32
import javax.inject.Inject

//This class is the adapter to the DBus Agent Interface

typealias Passkey = UInt32
typealias Entered = UInt16

class AgentIncomingEventAdapter @Inject constructor(
    private val connectionOwningComponent: ConnectionOwningComponent,
    private val logger : Logger
) : ConnectionDependingComponent {

    private companion object {
        const val TAG = "AgentIncomingEventAdapter"
        const val agentPath = "/pairing/agent"
    }

    private lateinit var systemConnection : DBusSystemConnection
    private lateinit var sessionConnection : DBusSessionConnection
    private lateinit var agentManager: AgentManager1

    override fun onSetup() {
        systemConnection = connectionOwningComponent.getSystemBusConnection()
        sessionConnection = connectionOwningComponent.getSessionBusConnection()

        sessionConnection.exportObject(agentPath, agent)

        agentManager = systemConnection.getAgentManager() ?: error("Couldn't get the AgentManager")
        agentManager.RegisterAgent(
            DBusPath(agentPath),
            "DisplayYesNo"
        )
    }

    override fun onCleanup() {
        try {
            agentManager.UnregisterAgent(DBusPath(agentPath))
        } catch (e : BluezDoesNotExistException) {
            logger.d(TAG, "Tried to unregister agent that didn't exist at $agentPath")
        }
        sessionConnection.unExportObject(agentPath)
    }

    private fun DBusSystemConnection.getAgentManager() : AgentManager1? {
        return this.getRemoteObject(
            "org.bluez",
            "/org/bluez",
            AgentManager1::class.java
        )
    }

    sealed class AgentEvent {

        object AgentReleased : AgentEvent()

        data class DisplayPassKey(
            val device : DBusPath,
            val passKey : Passkey,
            val entered: Entered
        ) : AgentEvent()

        //Called when DisplayPasskey or DisplayPinCode no longer need to be
        //shown. The consumer of the event flow should window a state machine
        //to drive the screens displayed.
        object Cancel : AgentEvent()
    }

    val incomingAgentEvents : SharedFlow<AgentEvent>
        get() = _incomingAgentEvents
    private val _incomingAgentEvents = MutableSharedFlow<AgentEvent>()

    //Keep track of which passkeys are okay to show, validate for connecting devices.
    private val passKeyMutex = Mutex()
    private val passKeyMap : MutableMap<DBusPath, Pair<Passkey, Entered>> = mutableMapOf()


    //https://git.kernel.org/pub/scm/bluetooth/bluez.git/tree/doc/agent-api.txt
    private val agent = object : Agent1 {

        override fun isRemote(): Boolean = false

        override fun getObjectPath(): String = agentPath

        override fun Release() {
            logger.i(TAG, "Release called on agent with path $agentPath")
            runBlocking {
                _incomingAgentEvents.emit(AgentEvent.AgentReleased)
            }
        }

        override fun RequestPinCode(_device: DBusPath?): String {
            //If we're pairing a bluetooth keyboard, the pincode is always "e39"
            return "e39"
        }

        override fun DisplayPinCode(_device: DBusPath?, _pincode: String?) {
            //https://www.ellisys.com/technology/een_bt07.pdf
            //I'm pretty sure that this is just for really old BT keyboards,
            //so I'm not supporting displaying it in the HMI.
            //If someone wants, they can pair with "e39"
            logger.i(TAG, "Display PinCode: device : $_device, pin: $_pincode")
        }

        override fun RequestPasskey(_device: DBusPath?): UInt32 {
            if (_device == null) {
                throw BluezCanceledException("_device was null")
            }

            return runBlocking {
                passKeyMutex.withLock {
                    if (passKeyMap.containsKey(_device)) {
                        passKeyMap[_device]!!.first
                    } else {
                        //Generate a number between 0-999_999
                        val passKey: Passkey = UInt32((0..999_999).random().toLong())
                        passKeyMap[_device] = Pair(passKey, UInt16(0))
                        passKey
                    }
                }
            }
        }

        override fun DisplayPasskey(_device: DBusPath?, _passkey: UInt32?, _entered: UInt16?) {
            logger.d(TAG, "Display PassKey: ${_passkey}, $_entered")

            //During the pairing process, this method can be called to update the entered value.
            runBlocking {
                passKeyMutex.withLock {
                    passKeyMap[_device!!] = passKeyMap[_device]!!.let { old ->
                        Pair(old.first, UInt16(old.second.toInt() + 1))
                    }
                }

                _incomingAgentEvents.emit(
                    AgentEvent.DisplayPassKey(
                        _device!!,
                        _passkey!!,
                        _entered!!
                    )
                )
            }
        }

        override fun RequestConfirmation(_device: DBusPath?, _passkey: UInt32?) {
            if (_device == null) {
                throw BluezRejectedException("_device was null")
            }
            if (_passkey == null) {
                throw BluezRejectedException("_passkey was null")
            }

            runBlocking {
                passKeyMutex.withLock {
                    if (!passKeyMap.containsKey(_device)) {
                        throw BluezRejectedException("No passkey stored for device $_device")
                    }

                    if (passKeyMap[_device]?.first != _passkey) {
                        throw BluezRejectedException("Incorrect passkey stored for device $_device")
                    }
                }
            }

            //Falling through to here is success
            return
        }

        override fun RequestAuthorization(_device: DBusPath?) {
            //Do nothing to allow authorization to everything.
        }

        override fun AuthorizeService(_device: DBusPath?, _uuid: String?) {
            //Do nothing to allow authorization to everything
        }

        override fun Cancel() {
            runBlocking {
                _incomingAgentEvents.emit(AgentEvent.Cancel)
            }
        }
    }
}