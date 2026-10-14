package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDefectPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newAdaptiveDefectPlan("code-242", "name-357")
        assertEquals(AdaptiveDefectPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newAdaptiveDefectPlan("code-242", "name-357")
        try {
            item.moveTo(AdaptiveDefectPlanStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDefectPlanStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newAdaptiveDefectPlan("code-242", "name-357")
        item.status = AdaptiveDefectPlanStatus.CLOSED
        assertFails("move") { item.moveTo(AdaptiveDefectPlanStatus.DRAFT) }
    }
}
