package com.acme.transport.freightpay.fuelsurcharge.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeMilestoneStore()
        val item = newFuelSurchargeMilestone("code-604", "label-989")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
