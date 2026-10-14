package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDisputeRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDisputeRollupStore()
        val item = newFallbackDisputeRollup("code-590", "title-762")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
