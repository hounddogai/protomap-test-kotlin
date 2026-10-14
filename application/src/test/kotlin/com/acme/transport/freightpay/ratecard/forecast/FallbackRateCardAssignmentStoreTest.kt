package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRateCardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRateCardAssignmentStore()
        val item = newFallbackRateCardAssignment("code-427", "name-42")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
