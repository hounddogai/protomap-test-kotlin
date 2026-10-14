package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelSurchargeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelSurchargeAdjustmentStore()
        val item = newTieredFuelSurchargeAdjustment("code-913", "name-420")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
