package com.acme.transport.customs.broker.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyBrokerReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyBrokerReservationStore()
        val item = newLegacyBrokerReservation("code-760", "label-701")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
