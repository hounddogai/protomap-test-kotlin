package com.acme.transport.customs.tariff.tariffprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTariffAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTariffAllocationStore()
        val item = newLegacyTariffAllocation("code-366", "name-803")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
