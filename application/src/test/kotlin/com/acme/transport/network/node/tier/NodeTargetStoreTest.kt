package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeTargetStore()
        val item = newNodeTarget("code-740", "name-262")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
