package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeEstimateTest {
    @Test
    fun `starts in idle`() {
        val item = newNodeEstimate("code-858", "label-72")
        assertEquals(NodeEstimateStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newNodeEstimate("code-858", "label-72")
        try {
            item.moveTo(NodeEstimateStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeEstimateStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newNodeEstimate("code-858", "label-72")
        item.status = NodeEstimateStatus.STOPPED
        assertFails("move") { item.moveTo(NodeEstimateStatus.IDLE) }
    }
}
