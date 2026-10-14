package com.acme.transport.emissions.fuelburn.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnQuotaStore()
        val item = newFuelBurnQuota("code-563", "title-446")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
