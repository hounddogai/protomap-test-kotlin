package com.acme.transport.fleetops.utilization.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackUtilizationPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newFallbackUtilizationPlan("code-585", "name-326")
        assertEquals(FallbackUtilizationPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newFallbackUtilizationPlan("code-585", "name-326")
        try {
            item.moveTo(FallbackUtilizationPlanStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackUtilizationPlanStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newFallbackUtilizationPlan("code-585", "name-326")
        item.status = FallbackUtilizationPlanStatus.CLOSED
        assertFails("move") { item.moveTo(FallbackUtilizationPlanStatus.DRAFT) }
    }
}
