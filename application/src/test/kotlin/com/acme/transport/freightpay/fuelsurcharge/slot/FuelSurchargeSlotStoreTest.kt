package com.acme.transport.freightpay.fuelsurcharge.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeSlotStore()
        val item = newFuelSurchargeSlot("code-745", "title-127")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
