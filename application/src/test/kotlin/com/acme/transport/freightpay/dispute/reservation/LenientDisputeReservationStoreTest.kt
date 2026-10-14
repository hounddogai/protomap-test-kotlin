package com.acme.transport.freightpay.dispute.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDisputeReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDisputeReservationStore()
        val item = newLenientDisputeReservation("code-569", "name-366")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
