package com.acme.transport.freightpay.audit.auditclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAuditTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAuditTargetStore()
        val item = newCappedAuditTarget("code-490", "name-299")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
