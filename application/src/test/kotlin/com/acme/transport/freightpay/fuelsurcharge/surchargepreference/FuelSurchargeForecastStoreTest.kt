package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeForecastStore()
        val item = newFuelSurchargeForecast("code-295", "name-728")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
