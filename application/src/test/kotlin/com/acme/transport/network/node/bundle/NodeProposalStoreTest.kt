package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeProposalStore()
        val item = newNodeProposal("code-748", "label-784")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
