package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditSummaryStore()
        val item = newAuditSummary("code-343", "name-969")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
