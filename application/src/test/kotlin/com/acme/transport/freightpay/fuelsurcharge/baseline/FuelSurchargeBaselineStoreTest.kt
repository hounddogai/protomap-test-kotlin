package com.acme.transport.freightpay.fuelsurcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeBaselineStore()
        val item = newFuelSurchargeBaseline("code-843", "title-576")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
