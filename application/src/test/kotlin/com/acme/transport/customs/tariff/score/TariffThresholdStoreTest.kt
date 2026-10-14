package com.acme.transport.customs.tariff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffThresholdStore()
        val item = newTariffThreshold("code-270", "name-718")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
