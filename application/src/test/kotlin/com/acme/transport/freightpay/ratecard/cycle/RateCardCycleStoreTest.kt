package com.acme.transport.freightpay.ratecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RateCardCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRateCardCycleStore()
        val item = newRateCardCycle("code-791", "label-684")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
