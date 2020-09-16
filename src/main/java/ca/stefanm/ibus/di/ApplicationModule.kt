package ca.stefanm.ibus.di

import ca.stefanm.ibus.lib.bluetooth.blueZdbus.CliTrackInfoPrinter
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.ScreenTrackInfoPrinter
import ca.stefanm.ibus.lib.bluetooth.blueZdbus.TrackInfoPrinter
import ca.stefanm.ibus.lib.bordmonitor.input.InputEvent
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.Mk4NavTextLengthConstraints
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TextLengthConstraints
import ca.stefanm.ibus.lib.bordmonitor.menu.painter.TvModuleTextLengthConstraints
import ca.stefanm.ibus.lib.hardwareDrivers.CliRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.RpiRelayReaderWriter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.JSerialCommsAdapter
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortReader
import ca.stefanm.ibus.lib.hardwareDrivers.ibus.SerialPortWriter
import ca.stefanm.ibus.lib.logging.Logger
import ca.stefanm.ibus.lib.logging.StdOutLogger
import ca.stefanm.ibus.lib.messages.IBusMessage
import ca.stefanm.ibus.lib.platform.DeviceConfiguration
import ca.stefanm.ibus.lib.platform.LaptopDeviceConfiguration
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel
import javax.inject.Named
import javax.inject.Singleton

@ExperimentalCoroutinesApi
@Module
class ApplicationModule {

    //TODO
    //TODO https://arunkumar.dev/introducing-scabbard-a-tool-to-visualize-dagger-2-dependency-graphs/

    companion object {
        //Messages sent to rest of car
        const val IBUS_MESSAGE_OUTPUT_CHANNEL = "IbusOutput"
    }

    @Provides
    @Singleton
    fun provideDeviceConfiguration() : DeviceConfiguration {
        return LaptopDeviceConfiguration()
    }

    @Provides
    @Singleton
    @Named(IBUS_MESSAGE_OUTPUT_CHANNEL)
    fun provideIbusOuptutChannel() : Channel<IBusMessage> = Channel(capacity = Channel.UNLIMITED)

    @Provides
    @Singleton
    fun provideCoroutineScope() : CoroutineScope = GlobalScope

    @Provides
    @Singleton
    fun provideCoroutineDispatcher() : CoroutineDispatcher = Dispatchers.IO

    @Provides
    fun provideTextLengthConstraints(deviceConfiguration: DeviceConfiguration) : TextLengthConstraints {
        return if (deviceConfiguration.displayDriver == DeviceConfiguration.DisplayDriver.TV_MODULE) {
            TvModuleTextLengthConstraints
        } else {
            Mk4NavTextLengthConstraints
        }
    }

    @Provides
    fun provideRelayReaderWriter(
        deviceConfiguration: DeviceConfiguration,
        cliRelayReaderWriter: CliRelayReaderWriter,
        rpiRelayReaderWriter: RpiRelayReaderWriter
    ) : RelayReaderWriter {
        return if (deviceConfiguration.isPi) {
            rpiRelayReaderWriter
        } else {
            cliRelayReaderWriter
        }
    }

    @Provides
    @Singleton
    fun provideSerialPortReader(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortReader = jSerialCommsAdapter

    @Provides
    @Singleton
    fun provideSerialPortWriter(jSerialCommsAdapter: JSerialCommsAdapter) : SerialPortWriter = jSerialCommsAdapter

    @Provides
    @Singleton
    fun providePairedPhone(deviceConfiguration: DeviceConfiguration) : DeviceConfiguration.PairedPhone = deviceConfiguration.pairedPhone

    @Provides
    @Singleton
    fun provideTrackPrinter(screenTrackInfoPrinter: ScreenTrackInfoPrinter) : TrackInfoPrinter = screenTrackInfoPrinter
//    fun provideTrackPrinter(cliTrackInfoPrinter: CliTrackInfoPrinter) : TrackInfoPrinter = cliTrackInfoPrinter

    @Provides
    @Singleton
    fun provideLogger() : Logger {
        return StdOutLogger()
    }
}