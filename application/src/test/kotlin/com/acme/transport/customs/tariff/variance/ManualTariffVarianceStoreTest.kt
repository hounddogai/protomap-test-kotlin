package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTariffVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTariffVarianceStore()
        val item = newManualTariffVariance("code-85", "name-308")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
