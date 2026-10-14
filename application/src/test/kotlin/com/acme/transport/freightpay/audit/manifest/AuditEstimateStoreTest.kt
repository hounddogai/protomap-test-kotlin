package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditEstimateStore()
        val item = newAuditEstimate("code-221", "title-639")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
