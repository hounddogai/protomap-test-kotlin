package com.acme.transport.freightpay.dispute.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeAuditStore()
        val item = newDisputeAudit("code-460", "name-397")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
