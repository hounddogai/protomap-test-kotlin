package com.acme.transport.fleetops.utilization.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedUtilizationRuleTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedUtilizationRule("code-366", "name-891")
        assertEquals(WeightedUtilizationRuleStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newWeightedUtilizationRule("code-366", "name-891")
        try {
            item.moveTo(WeightedUtilizationRuleStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedUtilizationRuleStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedUtilizationRule("code-366", "name-891")
        item.status = WeightedUtilizationRuleStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedUtilizationRuleStatus.DRAFT) }
    }
}
