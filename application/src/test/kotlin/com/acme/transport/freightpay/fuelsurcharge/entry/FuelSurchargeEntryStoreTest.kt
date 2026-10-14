package com.acme.transport.freightpay.fuelsurcharge.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeEntryStore()
        val item = newFuelSurchargeEntry("code-772", "title-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
