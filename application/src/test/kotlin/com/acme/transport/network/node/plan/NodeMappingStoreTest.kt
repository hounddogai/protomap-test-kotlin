package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeMappingStore()
        val item = newNodeMapping("code-786", "name-865")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
