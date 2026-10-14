package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFuelBurnNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFuelBurnNoticeStore()
        val item = newRegionalFuelBurnNotice("code-658", "name-168")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
