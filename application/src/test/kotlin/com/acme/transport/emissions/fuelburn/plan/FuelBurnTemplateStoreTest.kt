package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnTemplateStore()
        val item = newFuelBurnTemplate("code-307", "name-701")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
