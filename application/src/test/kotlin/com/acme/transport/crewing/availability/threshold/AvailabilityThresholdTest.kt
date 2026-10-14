package com.acme.transport.crewing.availability.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityThresholdTest {
    @Test
    fun `starts in draft`() {
        val item = newAvailabilityThreshold("code-197", "name-545")
        assertEquals(AvailabilityThresholdStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newAvailabilityThreshold("code-197", "name-545")
        try {
            item.moveTo(AvailabilityThresholdStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityThresholdStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAvailabilityThreshold("code-197", "name-545")
        item.status = AvailabilityThresholdStatus.CLOSED
        assertFails("move") { item.moveTo(AvailabilityThresholdStatus.DRAFT) }
    }
}
