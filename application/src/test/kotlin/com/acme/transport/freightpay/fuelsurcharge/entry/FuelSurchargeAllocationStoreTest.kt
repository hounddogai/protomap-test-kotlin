package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeAllocationStore()
        val item = newFuelSurchargeAllocation("code-157", "title-345")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
