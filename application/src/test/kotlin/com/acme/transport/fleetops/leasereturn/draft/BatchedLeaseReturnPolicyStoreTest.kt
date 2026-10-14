package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLeaseReturnPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLeaseReturnPolicyStore()
        val item = newBatchedLeaseReturnPolicy("code-440", "label-691")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
