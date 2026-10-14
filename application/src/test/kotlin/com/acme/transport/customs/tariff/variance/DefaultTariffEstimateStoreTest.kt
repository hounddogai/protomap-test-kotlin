package com.acme.transport.customs.tariff.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTariffEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTariffEstimateStore()
        val item = newDefaultTariffEstimate("code-179", "name-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
