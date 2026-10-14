package com.acme.transport.freightpay.dispute.disputemanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeMappingStore()
        val item = newDisputeMapping("code-454", "name-425")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
