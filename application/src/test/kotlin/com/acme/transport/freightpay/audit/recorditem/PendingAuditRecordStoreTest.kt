package com.acme.transport.freightpay.audit.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAuditRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAuditRecordStore()
        val item = newPendingAuditRecord("code-548", "name-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
