package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTariffSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTariffSlotStore()
        val item = newGlobalTariffSlot("code-819", "name-199")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
