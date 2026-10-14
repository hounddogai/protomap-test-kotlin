package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeRecordTest {
    @Test
    fun `starts in new`() {
        val item = newNodeRecord("code-539", "name-413")
        assertEquals(NodeRecordStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newNodeRecord("code-539", "name-413")
        try {
            item.moveTo(NodeRecordStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeRecordStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newNodeRecord("code-539", "name-413")
        item.status = NodeRecordStatus.FAILED
        assertFails("move") { item.moveTo(NodeRecordStatus.NEW) }
    }
}
