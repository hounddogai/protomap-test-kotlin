package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDisputeProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDisputeProfileStore()
        val item = newLegacyDisputeProfile("code-643", "label-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
