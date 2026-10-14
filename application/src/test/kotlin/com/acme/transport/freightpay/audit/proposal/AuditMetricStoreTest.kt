package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditMetricStore()
        val item = newAuditMetric("code-82", "title-601")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
