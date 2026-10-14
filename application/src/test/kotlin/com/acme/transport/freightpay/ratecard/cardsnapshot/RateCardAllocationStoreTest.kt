package com.acme.transport.freightpay.ratecard.cardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardAllocationStore()
        val item = newRateCardAllocation("code-602", "title-422")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
