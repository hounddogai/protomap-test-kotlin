package com.acme.transport.customs.tariff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffPreferenceStore()
        val item = newTariffPreference("code-489", "name-883")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
