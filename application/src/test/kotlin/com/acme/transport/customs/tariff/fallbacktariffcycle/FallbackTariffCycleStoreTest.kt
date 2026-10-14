package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTariffCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTariffCycleStore()
        val item = newFallbackTariffCycle("code-939", "title-357")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
