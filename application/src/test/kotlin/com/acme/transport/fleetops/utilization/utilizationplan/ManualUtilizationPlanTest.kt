package com.acme.transport.fleetops.utilization.utilizationplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualUtilizationPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualUtilizationPlan("code-757", "name-490")
        assertEquals(ManualUtilizationPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newManualUtilizationPlan("code-757", "name-490")
        try {
            item.moveTo(ManualUtilizationPlanStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualUtilizationPlanStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualUtilizationPlan("code-757", "name-490")
        item.status = ManualUtilizationPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualUtilizationPlanStatus.PROPOSED) }
    }
}
