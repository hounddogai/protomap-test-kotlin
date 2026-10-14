package com.acme.transport.network.node.nodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalNodePolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalNodePolicyStore()
        val item = newGlobalNodePolicy("code-493", "label-20")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
