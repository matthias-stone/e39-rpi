package ca.stefanm.ibus.car.platform

import ca.stefanm.ibus.car.bordmonitor.input.InputEvent
import ca.stefanm.ibus.car.di.ConfiguredCarComponent
import ca.stefanm.ibus.car.di.ConfiguredCarModule
import ca.stefanm.ibus.car.di.ConfiguredCarScope
import ca.stefanm.ibus.configuration.CarPlatformConfiguration
import ca.stefanm.ibus.configuration.LaptopDeviceConfiguration
import ca.stefanm.ibus.di.ApplicationModule
import ca.stefanm.ibus.di.ApplicationScope
import ca.stefanm.ibus.di.DaggerApplicationComponent
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import javax.inject.Inject
import javax.inject.Named

@ExperimentalCoroutinesApi
@ApplicationScope
class ConfigurablePlatform @Inject constructor() {

    private var runStatusViewer : ConfigurablePlatformServiceRunStatusViewer? = null
    private val _servicesRunning = MutableStateFlow<List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>>(
        listOf()
    )
    val servicesRunning : StateFlow<List<ConfigurablePlatformServiceRunStatusViewer.RunStatusRecordGroup>>
        get() = _servicesRunning.asStateFlow()

    var configurablePlatformServiceRunner: ConfigurablePlatformServiceRunner? = null
    var configuredCarComponent : ConfiguredCarComponent? = null

    var currentConfiguration : CarPlatformConfiguration? = null
        private set(value) {
            field = value
            runBlocking {
                if (value != null) {
                    _currentConfigurationFlow.emit(value)
                }
            }
        }

    private val _currentConfigurationFlow = MutableSharedFlow<CarPlatformConfiguration>(replay = 1)
    val currentConfigurationFlow : SharedFlow<CarPlatformConfiguration> = _currentConfigurationFlow

    private var serviceListJob : Job? = null

    fun run(initialConfiguration: CarPlatformConfiguration = LaptopDeviceConfiguration()) {
        onNewDeviceConfiguration(currentConfiguration ?: initialConfiguration)
    }

    fun stop() {
        configurablePlatformServiceRunner?.stopAll()
        configurablePlatformServiceRunner = null
        serviceListJob?.cancel()
        _servicesRunning.value = listOf()
    }


    fun onNewDeviceConfiguration(configuration: CarPlatformConfiguration) {
        //destroy and recreate the Platform.
        stop()

        configuredCarComponent = DaggerApplicationComponent.create()
            .configuredCarComponent(ConfiguredCarModule(configuration))

        configurablePlatformServiceRunner =
            configuredCarComponent?.configurablePlatformServiceRunner()

        configurablePlatformServiceRunner?.runAll()
        currentConfiguration = configuration

        runStatusViewer = ConfigurablePlatformServiceRunStatusViewer(
            configuredCarComponent!!.platformServiceList()
        )

        runStatusViewer!!.onNewConfiguredCar()
        serviceListJob = GlobalScope.launch {
            runStatusViewer!!.records.collect { _servicesRunning.value = it }
        }
    }
}

@ConfiguredCarScope
class ConfigurablePlatformServiceRunner @Inject constructor(
    private val list: PlatformServiceList
) {
    fun runAll() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onCreate()
            }
        }
    }

    fun stopAll() {
        list.list.forEach { group ->
            group.children.forEach { service ->
                service.onShutdown()
            }
        }
    }

    fun stopByName(serviceName : String) {
        findService(serviceName).onShutdown()
    }

    fun startByName(serviceName: String) {
        findService(serviceName).onCreate()
    }

    private fun findService(name: String) : PlatformService {
        return list.list.map { it.children }.flatten().first { it.name == name }
    }
}

class ConfigurablePlatformServiceRunStatusViewer internal constructor(
    private val platformServiceList: PlatformServiceList
){

    data class RunStatusRecordGroup(
        val name : String,
        val description: String,
        val children : List<RunStatusRecordService>
    )
    data class RunStatusRecordService(
        val name : String,
        val description : String,
        val runStatus : Flow<PlatformService.RunStatus>,
        val startService : () -> Unit,
        val stopService : () -> Unit
    )

    val _records : MutableStateFlow<List<RunStatusRecordGroup>> = MutableStateFlow(listOf())
    val records : StateFlow<List<RunStatusRecordGroup>> get() = _records


    internal fun onNewConfiguredCar() {
        //We need to cancel all the flows?

        val newList = platformServiceList.list.map { group ->
            RunStatusRecordGroup(
                name = group.name,
                description = group.description,
                children = group.children.map { service ->
                    RunStatusRecordService(
                        name = service.name,
                        description = service.description,
                        runStatus = flow { service.runStatusFlow.collect { emit(it) } },
                        startService = { service.onCreate() },
                        stopService = { service.onShutdown() }
                    )
                }
            )
        }
        _records.value = newList
    }
}