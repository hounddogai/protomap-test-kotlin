package com.acme.transport.freightpay.fuelsurcharge.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelSurchargeSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelSurchargeSnapshotStore()
        val item = newFuelSurchargeSnapshot("code-226", "title-594")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
