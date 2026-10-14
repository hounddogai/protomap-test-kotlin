package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAuditPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAuditPolicyStore()
        val item = newManualAuditPolicy("code-617", "title-123")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
