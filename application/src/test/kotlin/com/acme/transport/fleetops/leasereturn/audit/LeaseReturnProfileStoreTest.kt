package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnProfileStore()
        val item = newLeaseReturnProfile("code-713", "label-714")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
