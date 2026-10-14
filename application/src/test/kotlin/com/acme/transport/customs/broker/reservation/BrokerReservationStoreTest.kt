package com.acme.transport.customs.broker.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BrokerReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBrokerReservationStore()
        val item = newBrokerReservation("code-462", "title-941")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
