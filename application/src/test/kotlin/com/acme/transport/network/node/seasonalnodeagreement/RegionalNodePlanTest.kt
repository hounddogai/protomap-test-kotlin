package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalNodePlanTest {
    @Test
    fun `starts in idle`() {
        val item = newRegionalNodePlan("code-695", "title-666")
        assertEquals(RegionalNodePlanStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newRegionalNodePlan("code-695", "title-666")
        try {
            item.moveTo(RegionalNodePlanStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalNodePlanStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newRegionalNodePlan("code-695", "title-666")
        item.status = RegionalNodePlanStatus.STOPPED
        assertFails("move") { item.moveTo(RegionalNodePlanStatus.IDLE) }
    }
}
