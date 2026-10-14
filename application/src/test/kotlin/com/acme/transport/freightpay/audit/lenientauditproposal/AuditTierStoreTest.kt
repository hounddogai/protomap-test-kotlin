package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditTierStore()
        val item = newAuditTier("code-558", "label-398")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
