package com.acme.transport.freightpay.dispute.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeReservationStore()
        val item = newDisputeReservation("code-827", "title-541")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
