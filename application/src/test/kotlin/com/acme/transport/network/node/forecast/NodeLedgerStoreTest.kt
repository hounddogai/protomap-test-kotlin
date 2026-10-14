package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeLedgerStore()
        val item = newNodeLedger("code-632", "label-335")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
