package com.acme.transport.network.node.nodepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeBundleStore()
        val item = newNodeBundle("code-264", "label-451")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
