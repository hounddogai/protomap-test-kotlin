package com.acme.transport.freightpay.audit.auditbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredAuditBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredAuditBudgetStore()
        val item = newTieredAuditBudget("code-953", "title-264")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
