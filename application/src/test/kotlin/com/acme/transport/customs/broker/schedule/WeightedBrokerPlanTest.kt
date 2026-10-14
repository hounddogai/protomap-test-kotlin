package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedBrokerPlanTest {
    @Test
    fun `starts in draft`() {
        val item = newWeightedBrokerPlan("code-452", "name-399")
        assertEquals(WeightedBrokerPlanStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newWeightedBrokerPlan("code-452", "name-399")
        try {
            item.moveTo(WeightedBrokerPlanStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedBrokerPlanStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newWeightedBrokerPlan("code-452", "name-399")
        item.status = WeightedBrokerPlanStatus.CLOSED
        assertFails("move") { item.moveTo(WeightedBrokerPlanStatus.DRAFT) }
    }
}
