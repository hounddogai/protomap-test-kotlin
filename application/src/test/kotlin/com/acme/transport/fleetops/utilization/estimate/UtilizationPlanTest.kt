package com.acme.transport.fleetops.utilization.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class UtilizationPlanTest {
    @Test
    fun `starts in open`() {
        val item = newUtilizationPlan("code-482", "title-861")
        assertEquals(UtilizationPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newUtilizationPlan("code-482", "title-861")
        try {
            item.moveTo(UtilizationPlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(UtilizationPlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newUtilizationPlan("code-482", "title-861")
        item.status = UtilizationPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(UtilizationPlanStatus.OPEN) }
    }
}
