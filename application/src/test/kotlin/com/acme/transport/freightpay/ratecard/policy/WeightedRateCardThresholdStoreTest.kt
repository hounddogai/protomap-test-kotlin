package com.acme.transport.freightpay.ratecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRateCardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRateCardThresholdStore()
        val item = newWeightedRateCardThreshold("code-631", "name-918")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
