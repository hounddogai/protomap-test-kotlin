package com.acme.transport.freightpay.audit.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditPolicyStore()
        val item = newAuditPolicy("code-115", "title-156")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
