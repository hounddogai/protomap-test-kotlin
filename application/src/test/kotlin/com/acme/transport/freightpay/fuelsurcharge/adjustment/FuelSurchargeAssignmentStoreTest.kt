package com.acme.transport.freightpay.fuelsurcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeAssignmentStore()
        val item = newFuelSurchargeAssignment("code-578", "name-359")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
