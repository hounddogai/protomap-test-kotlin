package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardEstimateStore()
        val item = newRateCardEstimate("code-584", "name-532")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
