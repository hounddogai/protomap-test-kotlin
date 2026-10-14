package com.acme.transport.freightpay.fuelsurcharge.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeSegmentStore()
        val item = newFuelSurchargeSegment("code-740", "name-670")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
