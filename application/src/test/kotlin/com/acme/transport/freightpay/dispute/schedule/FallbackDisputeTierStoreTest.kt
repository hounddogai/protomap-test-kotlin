package com.acme.transport.freightpay.dispute.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDisputeTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDisputeTierStore()
        val item = newFallbackDisputeTier("code-873", "title-61")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
