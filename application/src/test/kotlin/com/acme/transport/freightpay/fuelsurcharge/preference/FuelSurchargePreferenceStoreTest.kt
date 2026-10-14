package com.acme.transport.freightpay.fuelsurcharge.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargePreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargePreferenceStore()
        val item = newFuelSurchargePreference("code-145", "title-203")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
