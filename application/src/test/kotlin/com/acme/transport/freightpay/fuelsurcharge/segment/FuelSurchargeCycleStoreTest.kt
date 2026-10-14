package com.acme.transport.freightpay.fuelsurcharge.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeCycleStore()
        val item = newFuelSurchargeCycle("code-163", "name-657")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
