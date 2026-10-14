package com.acme.transport.freightpay.fuelsurcharge.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeReservationStore()
        val item = newFuelSurchargeReservation("code-730", "name-637")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
