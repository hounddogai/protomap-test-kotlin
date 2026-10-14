package com.acme.transport.network.node.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeAuditStore()
        val item = newNodeAudit("code-14", "label-613")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
