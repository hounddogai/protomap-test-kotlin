package com.acme.transport.linehaul.tender.tenderestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderAuditStore()
        val item = newTenderAudit("code-442", "title-397")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
