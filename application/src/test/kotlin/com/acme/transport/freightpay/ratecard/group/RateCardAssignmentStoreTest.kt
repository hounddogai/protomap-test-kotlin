package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardAssignmentStore()
        val item = newRateCardAssignment("code-583", "title-750")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
