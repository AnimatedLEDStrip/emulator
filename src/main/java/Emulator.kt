package animatedledstrip.server

import animatedledstrip.animations.parameters.AbsoluteDistance
import animatedledstrip.animations.parameters.Equation
import animatedledstrip.colors.ColorContainer
import animatedledstrip.colors.ccpresets.Random
import animatedledstrip.leds.animationmanagement.*
import animatedledstrip.leds.colormanagement.getPixelActualColor
import animatedledstrip.leds.locationmanagement.Location
import javafx.event.EventHandler
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.shape.Circle
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.buffer
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import tornadofx.*
import kotlin.math.pow


/**
 * A GUI that shows an emulated LED strip using circles arranged in a spiral.
 *
 * The equation y = x^(1/2) is used to determine the polar coordinates for each circle to create a spiral.
 *
 * A button labeled "Test Animation" is located at the bottom of the window. This can be set to test different
 * animations by adding an animation call in the action lambda for the button (labeled with "Put animation
 * call to test here" in the code below).
 */
class WS281xEmulator : View("WS281x Emulator") {

//    inner class UpdateTimer : AnimationTimer() {
//        override fun handle(now: Long) {
//            getPixelColors()
//                .buffer(5)
//                .zip(circleList.asFlow()) { color, circle ->
//                    println(circle)
//                    circle.style {
//                        fill = Color.color(
//                            ((color shr 16 and 0xFF) / 255.0),
//                            ((color shr 8 and 0xFF) / 255.0),
//                            ((color and 0xFF) / 255.0),
//                            1.0,
//                        )
//                    }
//                }
//        }
//    }


    /**
     * Scaling for r component.
     */
    private val scale = 22.0

    /**
     * List of Circle instances (circle index == pixel index)
     */
    private val circleList = mutableListOf<Circle>()

    /**
     * X center of pane
     *
     * Takes furthest circle center (largest r) and adds 20
     */
    private val CENTER_X = scale * leds.numLEDs.toDouble().pow(0.5) + 20.0

    /**
     * Y center of pane
     *
     * Takes furthest circle center (largest r) and adds 20
     */
    private val CENTER_Y = scale * leds.numLEDs.toDouble().pow(0.5) + 20.0

    private var ledPane: Pane by singleAssign()

    private fun newCircle(index: Int, location: Location): Circle = Circle().apply {
        radius = 20.0                                   // Size of circle
        centerX = location.x
        centerY = location.y
        id = index.toString()                               // Create id for circle
        style {
            val color = leds.getPixelActualColor(index)
            fill = Color.color(
                ((color shr 16 and 0xFF) / 255.0),
                ((color shr 8 and 0xFF) / 255.0),
                ((color and 0xFF) / 255.0), 1.0
            )
        }

        onMouseClicked = EventHandler {
//            val newCircle = newCircle(id.toInt())
            replaceWith(this)
//            circleList[id.toInt()] = newCircle
            println("pixel: $index\t centerX: ~${centerX.toInt()}\t centerY: ~${centerY.toInt()}\t style: $style")
        }

    }

//    private val timer = UpdateTimer()

    init {
        for ((i, l) in pixelLocations.withIndex()) {
            circleList.add(newCircle(i, l))
        }

//        timer.start()

        /*
        * Start continuous loop in a separate thread that constantly updates
        * colors of circles.
        *
        * Probably should be done differently - currently the source of various
        * glitches in GUI, especially when colors are updating quickly.
        * (Technically you aren't supposed to change things in the GUI from
        * a different thread)
        */
        GlobalScope.launch {
            while (true) {
                getPixelColors()
                    .buffer(5)
                    .collectIndexed { index, color ->
                        runAsync {
                            circleList[index].style {
                                fill = Color.color(
                                    ((color shr 16 and 0xFF) / 255.0),
                                    ((color shr 8 and 0xFF) / 255.0),
                                    ((color and 0xFF) / 255.0),
                                    1.0,
                                )
                            }
//                            circleList[index].replaceWith(circleList[index])
                        }
                    }
//                runAsync {
//                    val randomCircle = circleList.random()
//                    randomCircle.replaceWith(randomCircle)
//                }
//                val pixels = leds.pixelTemporaryColorList
//                for (i in 0 until leds.numLEDs) {
//                    runAsync {
//                        circleList[i].style {
//                            fill = Color.color(
//                                ((pixels[i] shr 16 and 0xFF) / 255.0),
//                                ((pixels[i] shr 8 and 0xFF) / 255.0),
//                                ((pixels[i] and 0xFF) / 255.0), 1.0
//                            )
//                        }
//                    }
//                }
            }
        }

    }

    fun ColorContainer.toColor(): Color =
        Color.color((color shr 16 and 0xFF) / 255.0, (color shr 8 and 0xFF) / 255.0, (color and 0xFF) / 255.0)

    /**
     * Color of the pane background
     */
    private val backColor = ColorContainer(0x0)

    fun getPixelColors(): Flow<Int> = flow {
        for (i in 0 until leds.numLEDs) emit(leds.getPixelActualColor(i))
    }

    override val root = borderpane {

        style {
            backgroundColor += backColor.toColor()
        }

        center {
            style {
                backgroundColor += backColor.toColor()
                setMinSize(                                 // Set size of pane based on number of circles/pixels
                    scale * leds.numLEDs.toDouble().pow(0.5) * 2 + 50.0,
                    scale * leds.numLEDs.toDouble().pow(0.5) * 2 + 50.0
                )
            }
            ledPane = pane {
                for (i in 0 until leds.numLEDs) {   // Add circles to pane
                    this += circleList[i]
                }
            }
        }

        bottom {
            gridpane {
                row {
//                    button("Alternate") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Alternate").addColor(0xFF).addColor(0xFFFF))
//                            }
//                        }
//                    }
//                    button("Sparkle") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Sparkle").color(0xFF))
//                            }
//                        }
//                    }
//                    button("Wipe 2D") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Wipe2D")
//                                                                                   .color(ColorContainer.Random)
//                                                                                   .spacing(10)
//                                                                                   .distance(1000))
//                            }
//                        }
//                    }

                    button("Ripple") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Ripple")
                                        .color(ColorContainer.Random)
                                        .locationParam("center", Location(400, 350))
                                )
                            }
                        }
                    }
                    button("Save wave") {
                        action {
                            ledServer.addSavedAnimation(
                                AnimationToRunParams("Wave")
                                    .id("savedwave")
                                    .color(ColorContainer.Random)
//                                        .rotationParam("rotation", DegreesRotation(zRotation = 180.0))
                                    .doubleParam("movementPerIteration", 10.0)
                                    .intParam("interMovementDelay", 30)
                            )
                        }
                    }
                    button("Start wave") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(ledServer.savedAnimations["savedwave"]!!).params
                            }
                        }
                    }
                    button("Wave") {
                        action {
                            GlobalScope.launch {

                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Wave")
                                        .color(ColorContainer.Random)
//                                        .rotationParam("rotation", DegreesRotation(zRotation = 180.0))
                                        .doubleParam("movementPerIteration", 10.0)
                                        .intParam("interMovementDelay", 30)
                                )
                            }
                        }
                    }
                    button("Plane Run") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Plane Run")
                                        .color(ColorContainer.Random)
                                        .doubleParam("movementPerIteration", 10.0)
                                        .intParam("interMovementDelay", 30)
                                )
                            }
                        }
                    }
                    button("x^2") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Meteor")
                                        .equationParam("lineEquation", Equation(0.0, 0.0, 0.01))
                                        .distanceParam("offset", AbsoluteDistance(350, 100))
                                        .intParam("interMovementDelay", 30)
                                        .doubleParam("maximumInfluence", 100.0)
                                        .doubleParam("movementPerIteration", 10.0)
                                        .addColor(ColorContainer.Random)
                                )
                            }
                        }
                    }
                    button("x^3") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Meteor")
                                        .equationParam("lineEquation", Equation(0.0, 0.0, 0.0, 0.00005))
                                        .distanceParam("offset", AbsoluteDistance(350, 350))
                                        .intParam("interMovementDelay", 30)
                                        .doubleParam("maximumInfluence", 100.0)
                                        .doubleParam("movementPerIteration", 10.0)
                                        .addColor(ColorContainer.Random)
                                )
                            }
                        }
                    }
                    button("Runway") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Runway Lights")
                                        .equationParam("lineEquation", Equation(0.0, 1.0))
//                                        .distanceParam("offset", AbsoluteDistance(350, 350))
                                        .intParam("interMovementDelay", 30)
                                        .doubleParam("maximumInfluence", 50.0)
                                        .doubleParam("movementPerIteration", 25.0)
                                        .doubleParam("spacing", 200.0)
                                        .intParam("interMovementDelay", 100)
                                        .addColor(ColorContainer.Random)
                                )
                            }
                        }
                    }
                    button("Color") {
                        action {
                            GlobalScope.launch {
                                // Put animation call to test here
                                ledServer.leds.animationManager.startAnimation(
                                    AnimationToRunParams("Color")
                                        .color(ColorContainer.Random)
                                )
                            }
                        }
                    }

//                    button("Heap") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Meteor").color(
//                                    ColorContainer(
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                    )))
//                            }
//                        }
//                    }
//                    button("Parallel") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Quick Sort Parallel").color(
//                                    ColorContainer(
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                    )))
//                                SocketConnections.sendData(Message("A new message"))
//                            }
//                        }
//                    }
//                    button("Sequential") {
//                        action {
//                            GlobalScope.launch {
//                                // Put animation call to test here
//                                ledServer.leds.animationManager.startAnimation(AnimationToRunParams("Quick Sort Sequential").color(
//                                    ColorContainer(
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                        ColorContainer.Random,
//                                    )))
//                            }
//                        }
//                    }
                }
            }
        }
    }
}
