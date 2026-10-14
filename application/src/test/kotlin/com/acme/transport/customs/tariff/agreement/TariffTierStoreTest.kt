package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffTierStore()
        val item = newTariffTier("code-114", "title-101")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
