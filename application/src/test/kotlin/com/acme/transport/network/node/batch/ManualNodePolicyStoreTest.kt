package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualNodePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualNodePolicyStore()
        val item = newManualNodePolicy("code-261", "title-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
