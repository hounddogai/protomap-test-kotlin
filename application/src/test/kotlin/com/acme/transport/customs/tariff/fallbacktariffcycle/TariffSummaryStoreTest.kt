package com.acme.transport.customs.tariff.fallbacktariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffSummaryStore()
        val item = newTariffSummary("code-80", "name-809")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
