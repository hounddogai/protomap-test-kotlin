package com.acme.transport.freightpay.dispute.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDisputeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDisputeAuditStore()
        val item = newLenientDisputeAudit("code-568", "title-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
