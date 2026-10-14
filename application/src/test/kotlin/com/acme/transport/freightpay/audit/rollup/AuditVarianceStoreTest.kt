package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditVarianceStore()
        val item = newAuditVariance("code-116", "title-518")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
