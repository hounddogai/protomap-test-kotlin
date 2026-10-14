package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnDigestStore()
        val item = newFuelBurnDigest("code-615", "name-919")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
