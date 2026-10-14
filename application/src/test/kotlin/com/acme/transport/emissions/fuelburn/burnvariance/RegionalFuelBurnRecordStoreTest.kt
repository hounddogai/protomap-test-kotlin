package com.acme.transport.emissions.fuelburn.burnvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFuelBurnRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFuelBurnRecordStore()
        val item = newRegionalFuelBurnRecord("code-898", "title-846")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
