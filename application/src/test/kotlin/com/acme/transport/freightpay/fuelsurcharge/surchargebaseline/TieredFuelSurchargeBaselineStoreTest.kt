package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredFuelSurchargeBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredFuelSurchargeBaselineStore()
        val item = newTieredFuelSurchargeBaseline("code-230", "label-561")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
