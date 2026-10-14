package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRateCardQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRateCardQuotaStore()
        val item = newWeightedRateCardQuota("code-757", "name-771")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
