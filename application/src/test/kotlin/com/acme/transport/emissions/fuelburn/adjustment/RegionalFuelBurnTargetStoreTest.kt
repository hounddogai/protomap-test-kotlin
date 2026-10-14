package com.acme.transport.emissions.fuelburn.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFuelBurnTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFuelBurnTargetStore()
        val item = newRegionalFuelBurnTarget("code-676", "title-987")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
