package com.acme.transport.freightpay.ratecard.cardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRateCardForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRateCardForecastStore()
        val item = newLenientRateCardForecast("code-510", "title-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
