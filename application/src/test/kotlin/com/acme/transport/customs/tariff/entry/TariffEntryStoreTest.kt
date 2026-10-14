package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffEntryStore()
        val item = newTariffEntry("code-226", "label-773")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
