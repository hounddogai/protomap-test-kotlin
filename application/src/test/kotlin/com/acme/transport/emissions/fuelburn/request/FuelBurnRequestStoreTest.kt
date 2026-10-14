package com.acme.transport.emissions.fuelburn.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnRequestStore()
        val item = newFuelBurnRequest("code-875", "name-276")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
