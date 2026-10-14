package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditAllocationStore()
        val item = newAuditAllocation("code-394", "title-115")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
