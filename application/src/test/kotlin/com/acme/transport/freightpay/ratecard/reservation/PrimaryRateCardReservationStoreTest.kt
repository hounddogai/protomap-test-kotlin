package com.acme.transport.freightpay.ratecard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRateCardReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRateCardReservationStore()
        val item = newPrimaryRateCardReservation("code-681", "label-231")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
