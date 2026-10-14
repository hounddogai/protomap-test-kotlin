package com.acme.transport.linehaul.relay.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRelayPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRelayPolicyStore()
        val item = newBatchedRelayPolicy("code-274", "title-800")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
