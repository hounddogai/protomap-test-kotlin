package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalNodePlanTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalNodePlan("code-768", "label-888")
        assertEquals(SeasonalNodePlanStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newSeasonalNodePlan("code-768", "label-888")
        try {
            item.moveTo(SeasonalNodePlanStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalNodePlanStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalNodePlan("code-768", "label-888")
        item.status = SeasonalNodePlanStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalNodePlanStatus.NEW) }
    }
}
