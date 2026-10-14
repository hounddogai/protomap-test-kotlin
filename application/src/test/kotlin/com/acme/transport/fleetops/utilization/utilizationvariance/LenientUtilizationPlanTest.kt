package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientUtilizationPlanTest {
    @Test
    fun `starts in proposed`() {
        val item = newLenientUtilizationPlan("code-157", "title-210")
        assertEquals(LenientUtilizationPlanStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLenientUtilizationPlan("code-157", "title-210")
        try {
            item.moveTo(LenientUtilizationPlanStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientUtilizationPlanStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLenientUtilizationPlan("code-157", "title-210")
        item.status = LenientUtilizationPlanStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LenientUtilizationPlanStatus.PROPOSED) }
    }
}
