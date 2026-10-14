package com.acme.transport.linehaul.trailer.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerThresholdTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        assertEquals(WeightedTrailerThresholdStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        try {
            item.moveTo(WeightedTrailerThresholdStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerThresholdStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedTrailerThreshold("code-434", "name-758")
        item.status = WeightedTrailerThresholdStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedTrailerThresholdStatus.REQUESTED) }
    }
}
