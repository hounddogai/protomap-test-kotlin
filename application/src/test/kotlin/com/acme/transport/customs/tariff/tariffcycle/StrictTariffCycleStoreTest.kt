package com.acme.transport.customs.tariff.tariffcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTariffCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTariffCycleStore()
        val item = newStrictTariffCycle("code-670", "title-209")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
