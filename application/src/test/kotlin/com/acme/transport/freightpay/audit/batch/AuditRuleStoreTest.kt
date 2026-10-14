package com.acme.transport.freightpay.audit.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditRuleStore()
        val item = newAuditRule("code-34", "title-928")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
