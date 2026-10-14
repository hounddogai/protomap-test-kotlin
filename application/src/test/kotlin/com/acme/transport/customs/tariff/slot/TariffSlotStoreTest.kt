package com.acme.transport.customs.tariff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffSlotStore()
        val item = newTariffSlot("code-707", "label-499")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
