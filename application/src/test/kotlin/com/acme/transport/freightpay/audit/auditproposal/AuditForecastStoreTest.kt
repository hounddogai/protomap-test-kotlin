package com.acme.transport.freightpay.audit.auditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditForecastStore()
        val item = newAuditForecast("code-307", "label-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
