package com.acme.transport.network.node.weightednodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeRecordStore()
        val item = newNodeRecord("code-539", "name-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
