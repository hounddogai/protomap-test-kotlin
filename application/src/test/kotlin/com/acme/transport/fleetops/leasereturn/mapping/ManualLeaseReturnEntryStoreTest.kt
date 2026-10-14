package com.acme.transport.fleetops.leasereturn.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualLeaseReturnEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualLeaseReturnEntryStore()
        val item = newManualLeaseReturnEntry("code-442", "label-223")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
