package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardTierStore()
        val item = newRateCardTier("code-757", "title-926")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
