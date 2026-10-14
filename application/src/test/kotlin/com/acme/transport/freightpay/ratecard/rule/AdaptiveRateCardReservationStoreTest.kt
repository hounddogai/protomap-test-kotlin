package com.acme.transport.freightpay.ratecard.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRateCardReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRateCardReservationStore()
        val item = newAdaptiveRateCardReservation("code-129", "name-869")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
