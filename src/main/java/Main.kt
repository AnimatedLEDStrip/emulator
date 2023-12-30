package animatedledstrip.server

import animatedledstrip.leds.emulation.EmulatedWS281x
import animatedledstrip.leds.locationmanagement.Location
import animatedledstrip.leds.stripmanagement.LEDStrip
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.apache.commons.cli.DefaultParser
import tornadofx.*
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin

class EmulatorApp : App(WS281xEmulator::class)

lateinit var ledServer: AnimatedLEDStripServer<EmulatedWS281x>
val leds: LEDStrip
    get() = ledServer.leds

val scale = 22.0

val pixelLocations = mutableListOf<Location>()

fun main(args: Array<String>) {
    val numLEDs = DefaultParser().parse(options, args).getOptionValue("n").toInt()
    val centerOffset = scale * numLEDs.toDouble().pow(0.5) + 20.0

    for (i in 0 until numLEDs) {
        val t = 2.5 * (i + 3).toDouble().pow(0.5)          // t-component
        val r = scale * (i + 3).toDouble().pow(0.5)        // r-component
        val x = r * cos(t) + centerOffset // Convert polar coordinates to cartesian x
        val y = r * sin(t) + centerOffset // Convert polar coordinates to cartesian y
        pixelLocations.add(Location(x, y))
    }

    ledServer = AnimatedLEDStripServer(args, EmulatedWS281x::class, pixelLocations)
    GlobalScope.launch {
        ledServer.start().waitUntilStop()
    }
    launch<EmulatorApp>()
}
