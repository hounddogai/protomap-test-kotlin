package com.acme.transport.fleetops.leasereturn.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LeaseReturnAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLeaseReturnAuditStore()
        val item = newLeaseReturnAudit("code-360", "label-522")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
