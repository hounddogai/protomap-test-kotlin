package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeAdjustmentStore()
        val item = newFuelSurchargeAdjustment("code-423", "name-642")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
