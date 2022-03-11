package kr.fromwhy.menuselector.actor

import android.graphics.Bitmap
import android.util.Log

public class Wheel : Thread {

    interface WheelListener {
        fun newImage(img: Int): Unit;
    }

    private var images: ArrayList<Int>
    private var wheelListener: WheelListener
    private var frameDuration: Long = 0
    private var startIn: Long = 0
    private var isStarted: Boolean = true

    public var currentIndex: Int = 0

    constructor(images: ArrayList<Int>, wheelListener: WheelListener, frameDuration: Long, startIn: Long) {
        this.images = images
        this.wheelListener = wheelListener
        this.frameDuration = frameDuration
        this.startIn = startIn
        this.isStarted = true
    }

    public fun nextImage(): Unit {
        currentIndex++

        if(currentIndex == images.size) {
            currentIndex = 0
        }
    }

    public fun stopWheel(): Unit {
        isStarted = false
    }

    public fun isStarted(): Boolean {
        return isStarted
    }

    override fun run() {
        try {
            Thread.sleep(startIn)
        } catch(e: InterruptedException) {
            Log.e("Wheel", e.message.toString())
        }

        while(isStarted) {
            try {
                Thread.sleep(frameDuration)
            } catch(e: InterruptedException) {
                Log.e("Wheel", e.message.toString())
            }

            nextImage()

            wheelListener.newImage(images[currentIndex])
        }
    }
}