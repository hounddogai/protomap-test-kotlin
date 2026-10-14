package com.acme.transport.freightpay.audit.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAuditBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAuditBaselineStore()
        val item = newDefaultAuditBaseline("code-326", "title-25")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
