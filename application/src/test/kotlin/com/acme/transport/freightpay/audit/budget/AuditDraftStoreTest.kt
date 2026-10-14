package com.acme.transport.freightpay.audit.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditDraftStore()
        val item = newAuditDraft("code-225", "name-122")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
