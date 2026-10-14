package com.acme.transport.freightpay.fuelsurcharge.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeVarianceStore()
        val item = newFuelSurchargeVariance("code-122", "label-610")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
