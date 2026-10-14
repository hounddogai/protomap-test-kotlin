package com.acme.transport.freightpay.ratecard.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRateCardBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRateCardBaselineStore()
        val item = newAdaptiveRateCardBaseline("code-804", "name-18")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
