package com.acme.transport.kit.numbers

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails

class NumbersTest {
    @Test
    fun `clamps into range`() {
        assertEquals(10, clamp(12, 0, 10), "high")
        assertEquals(0, clamp(-3, 0, 10), "low")
        assertEquals(25.0, percent(1.0, 4.0), "percent")
    }

    @Test
    fun `finds the median by rank`() {
        assertEquals(3.0, percentile(mutableListOf<Double>(5.0, 1.0, 3.0), 50.0), "median")
        assertEquals(0.0, mean(mutableListOf<Double>()), "empty mean")
        assertFails("mismatched weights") { weightedMean(mutableListOf<Double>(1.0), mutableListOf<Double>()) }
    }
}
