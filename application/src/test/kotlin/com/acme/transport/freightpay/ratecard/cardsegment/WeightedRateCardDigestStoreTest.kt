package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRateCardDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRateCardDigestStore()
        val item = newWeightedRateCardDigest("code-505", "title-970")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
