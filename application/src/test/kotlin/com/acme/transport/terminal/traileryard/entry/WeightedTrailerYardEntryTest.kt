package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedTrailerYardEntryTest {
    @Test
    fun `starts in planned`() {
        val item = newWeightedTrailerYardEntry("code-454", "label-788")
        assertEquals(WeightedTrailerYardEntryStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newWeightedTrailerYardEntry("code-454", "label-788")
        try {
            item.moveTo(WeightedTrailerYardEntryStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedTrailerYardEntryStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newWeightedTrailerYardEntry("code-454", "label-788")
        item.status = WeightedTrailerYardEntryStatus.CANCELLED
        assertFails("move") { item.moveTo(WeightedTrailerYardEntryStatus.PLANNED) }
    }
}
