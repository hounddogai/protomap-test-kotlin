package com.acme.transport.fleetops.leasereturn.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLeaseReturnAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLeaseReturnAuditStore()
        val item = newLenientLeaseReturnAudit("code-16", "title-669")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
