package com.acme.transport.fleetops.utilization.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryUtilizationPlanTest {
    @Test
    fun `starts in idle`() {
        val item = newPrimaryUtilizationPlan("code-496", "label-993")
        assertEquals(PrimaryUtilizationPlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to draining`() {
        val item = newPrimaryUtilizationPlan("code-496", "label-993")
        try {
            item.moveTo(PrimaryUtilizationPlanStatus.DRAINING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryUtilizationPlanStatus.DRAINING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newPrimaryUtilizationPlan("code-496", "label-993")
        item.status = PrimaryUtilizationPlanStatus.STOPPED
        assertFails("move") { item.moveTo(PrimaryUtilizationPlanStatus.IDLE) }
    }
}
