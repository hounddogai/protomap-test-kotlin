package com.acme.transport.fleetops.leasereturn.returnaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnSummaryStore()
        val item = newLeaseReturnSummary("code-744", "label-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
