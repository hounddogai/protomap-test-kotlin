package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDisputeRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDisputeRequestStore()
        val item = newLegacyDisputeRequest("code-891", "label-548")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
