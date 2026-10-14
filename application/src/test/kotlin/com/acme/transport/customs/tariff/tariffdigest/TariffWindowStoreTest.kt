package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffWindowStore()
        val item = newTariffWindow("code-106", "label-936")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
