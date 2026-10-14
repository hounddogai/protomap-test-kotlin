package com.acme.transport.fleetops.leasereturn.returnschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLeaseReturnRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLeaseReturnRuleStore()
        val item = newBatchedLeaseReturnRule("code-249", "title-169")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
