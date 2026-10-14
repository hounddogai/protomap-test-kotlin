package com.acme.transport.network.node.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NodeManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newNodeManifest("code-185", "title-965")
        assertEquals(NodeManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newNodeManifest("code-185", "title-965")
        try {
            item.moveTo(NodeManifestStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NodeManifestStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newNodeManifest("code-185", "title-965")
        item.status = NodeManifestStatus.CLOSED
        assertFails("move") { item.moveTo(NodeManifestStatus.DRAFT) }
    }
}
