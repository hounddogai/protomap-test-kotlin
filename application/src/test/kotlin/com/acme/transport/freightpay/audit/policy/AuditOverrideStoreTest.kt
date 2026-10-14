package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditOverrideStore()
        val item = newAuditOverride("code-301", "label-792")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
