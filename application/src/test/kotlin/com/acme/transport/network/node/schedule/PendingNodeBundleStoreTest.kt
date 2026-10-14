package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingNodeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingNodeBundleStore()
        val item = newPendingNodeBundle("code-849", "name-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
