package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeTemplateStore()
        val item = newNodeTemplate("code-665", "name-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
