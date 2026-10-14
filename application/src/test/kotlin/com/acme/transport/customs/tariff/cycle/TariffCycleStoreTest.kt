package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffCycleStore()
        val item = newTariffCycle("code-436", "label-598")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
