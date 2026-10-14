package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRateCardReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRateCardReservationStore()
        val item = newStrictRateCardReservation("code-955", "label-455")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
