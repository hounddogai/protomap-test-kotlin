package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardThresholdStore()
        val item = newRateCardThreshold("code-3", "title-703")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
