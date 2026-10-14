package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditClaimStore()
        val item = newAuditClaim("code-2", "label-698")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
