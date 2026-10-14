package com.acme.transport.freightpay.audit.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAuditSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAuditSlotStore()
        val item = newRegionalAuditSlot("code-424", "title-700")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
