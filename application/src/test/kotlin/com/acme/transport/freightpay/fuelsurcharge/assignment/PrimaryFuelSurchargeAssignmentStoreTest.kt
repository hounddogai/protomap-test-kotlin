package com.acme.transport.freightpay.fuelsurcharge.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryFuelSurchargeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryFuelSurchargeAssignmentStore()
        val item = newPrimaryFuelSurchargeAssignment("code-823", "label-122")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
