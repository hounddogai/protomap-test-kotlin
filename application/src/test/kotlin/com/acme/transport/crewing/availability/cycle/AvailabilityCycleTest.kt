package com.acme.transport.crewing.availability.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newAvailabilityCycle("code-48", "name-690")
        assertEquals(AvailabilityCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newAvailabilityCycle("code-48", "name-690")
        try {
            item.moveTo(AvailabilityCycleStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityCycleStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newAvailabilityCycle("code-48", "name-690")
        item.status = AvailabilityCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(AvailabilityCycleStatus.PLANNED) }
    }
}
