package com.acme.transport.freightpay.fuelsurcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeSummaryStore()
        val item = newFuelSurchargeSummary("code-789", "label-892")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
