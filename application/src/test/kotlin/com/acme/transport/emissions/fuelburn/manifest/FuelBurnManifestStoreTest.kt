package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnManifestStore()
        val item = newFuelBurnManifest("code-835", "label-772")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
