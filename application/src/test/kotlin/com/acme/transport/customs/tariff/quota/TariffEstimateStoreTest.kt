package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffEstimateStore()
        val item = newTariffEstimate("code-803", "name-943")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
