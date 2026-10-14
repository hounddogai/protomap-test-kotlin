package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodePolicyStore()
        val item = newNodePolicy("code-927", "title-559")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
