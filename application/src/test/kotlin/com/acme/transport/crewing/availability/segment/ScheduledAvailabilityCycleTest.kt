package com.acme.transport.crewing.availability.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledAvailabilityCycleTest {
    @Test
    fun `starts in planned`() {
        val item = newScheduledAvailabilityCycle("code-697", "name-332")
        assertEquals(ScheduledAvailabilityCycleStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to cancelled`() {
        val item = newScheduledAvailabilityCycle("code-697", "name-332")
        try {
            item.moveTo(ScheduledAvailabilityCycleStatus.CANCELLED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledAvailabilityCycleStatus.CANCELLED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newScheduledAvailabilityCycle("code-697", "name-332")
        item.status = ScheduledAvailabilityCycleStatus.CANCELLED
        assertFails("move") { item.moveTo(ScheduledAvailabilityCycleStatus.PLANNED) }
    }
}
