package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardBaselineStore()
        val item = newRateCardBaseline("code-390", "label-569")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
