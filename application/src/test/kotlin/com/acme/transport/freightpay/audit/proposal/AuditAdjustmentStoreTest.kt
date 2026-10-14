package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditAdjustmentStore()
        val item = newAuditAdjustment("code-246", "name-447")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
