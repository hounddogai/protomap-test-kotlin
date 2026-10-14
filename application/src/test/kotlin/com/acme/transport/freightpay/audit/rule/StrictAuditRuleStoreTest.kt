package com.acme.transport.freightpay.audit.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAuditRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAuditRuleStore()
        val item = newStrictAuditRule("code-925", "label-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
