package com.acme.transport.crewing.availability.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AvailabilityPlanTest {
    @Test
    fun `starts in new`() {
        val item = newAvailabilityPlan("code-134", "label-198")
        assertEquals(AvailabilityPlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newAvailabilityPlan("code-134", "label-198")
        try {
            item.moveTo(AvailabilityPlanStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AvailabilityPlanStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAvailabilityPlan("code-134", "label-198")
        item.status = AvailabilityPlanStatus.FAILED
        assertFails("move") { item.moveTo(AvailabilityPlanStatus.NEW) }
    }
}
