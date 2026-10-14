package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeWindowStore()
        val item = newNodeWindow("code-503", "name-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
