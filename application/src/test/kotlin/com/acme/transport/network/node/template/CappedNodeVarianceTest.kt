package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedNodeVarianceTest {
    @Test
    fun `starts in idle`() {
        val item = newCappedNodeVariance("code-532", "name-670")
        assertEquals(CappedNodeVarianceStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newCappedNodeVariance("code-532", "name-670")
        try {
            item.moveTo(CappedNodeVarianceStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedNodeVarianceStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newCappedNodeVariance("code-532", "name-670")
        item.status = CappedNodeVarianceStatus.STOPPED
        assertFails("move") { item.moveTo(CappedNodeVarianceStatus.IDLE) }
    }
}
