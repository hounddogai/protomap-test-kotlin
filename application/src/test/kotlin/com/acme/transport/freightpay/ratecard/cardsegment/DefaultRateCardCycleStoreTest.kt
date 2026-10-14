package com.acme.transport.freightpay.ratecard.cardsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRateCardCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRateCardCycleStore()
        val item = newDefaultRateCardCycle("code-172", "name-497")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
