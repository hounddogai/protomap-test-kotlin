package com.acme.transport.freightpay.audit.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditScoreStore()
        val item = newAuditScore("code-228", "label-694")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
