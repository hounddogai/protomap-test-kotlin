package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyNodePlanTest {
    @Test
    fun `starts in planned`() {
        val item = newLegacyNodePlan("code-365", "name-950")
        assertEquals(LegacyNodePlanStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newLegacyNodePlan("code-365", "name-950")
        try {
            item.moveTo(LegacyNodePlanStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyNodePlanStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newLegacyNodePlan("code-365", "name-950")
        item.status = LegacyNodePlanStatus.CANCELLED
        assertFails("move") { item.moveTo(LegacyNodePlanStatus.PLANNED) }
    }
}
