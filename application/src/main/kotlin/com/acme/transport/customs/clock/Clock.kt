package com.acme.transport.customs.clock

// Clocks, so code that reads the time can be tested.

/** Clock tells the current time in Unix seconds. */
interface Clock {
    fun now(): Int
}

/** SystemClock reads the wall clock. */
class SystemClock : Clock {
    override fun now(): Int = (System.currentTimeMillis() / 1000).toInt()
}

/** FixedClock always tells the same time. */
data class FixedClock(var at: Int) : Clock {
    override fun now(): Int = at
}

/** OffsetClock runs ahead of (or behind) another clock by a fixed number of seconds. */
data class OffsetClock(var base: Clock, var offset: Int) : Clock {
    override fun now(): Int = base.now() + offset
}

/** StepClock advances by step seconds every time it is read. */
data class StepClock(var current: Int, var step: Int) : Clock {
    override fun now(): Int {
        val at = current
        current = current + step
        return at
    }
}

fun system(): Clock = SystemClock()
