package com.acme.transport.customs.tariff.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffGroupStore()
        val item = newTariffGroup("code-431", "name-21")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
