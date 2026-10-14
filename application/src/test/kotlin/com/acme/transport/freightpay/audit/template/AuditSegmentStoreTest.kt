package com.acme.transport.freightpay.audit.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditSegmentStore()
        val item = newAuditSegment("code-87", "title-857")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
