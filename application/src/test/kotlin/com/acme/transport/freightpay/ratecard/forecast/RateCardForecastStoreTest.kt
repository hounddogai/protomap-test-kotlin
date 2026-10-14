package com.acme.transport.freightpay.ratecard.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardForecastStore()
        val item = newRateCardForecast("code-217", "name-506")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
