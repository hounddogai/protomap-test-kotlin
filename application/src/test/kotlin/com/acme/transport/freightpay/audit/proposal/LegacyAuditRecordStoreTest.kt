package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyAuditRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyAuditRecordStore()
        val item = newLegacyAuditRecord("code-602", "label-915")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
