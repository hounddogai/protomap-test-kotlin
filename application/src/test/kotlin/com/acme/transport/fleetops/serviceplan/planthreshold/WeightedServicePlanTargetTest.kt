package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedServicePlanTargetTest {
    @Test
    fun `starts in open`() {
        val item = newWeightedServicePlanTarget("code-969", "label-619")
        assertEquals(WeightedServicePlanTargetStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newWeightedServicePlanTarget("code-969", "label-619")
        try {
            item.moveTo(WeightedServicePlanTargetStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedServicePlanTargetStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newWeightedServicePlanTarget("code-969", "label-619")
        item.status = WeightedServicePlanTargetStatus.ARCHIVED
        assertFails("move") { item.moveTo(WeightedServicePlanTargetStatus.OPEN) }
    }
}
