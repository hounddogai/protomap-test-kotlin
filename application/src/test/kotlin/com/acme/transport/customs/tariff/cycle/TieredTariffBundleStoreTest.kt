package com.acme.transport.customs.tariff.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTariffBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTariffBundleStore()
        val item = newTieredTariffBundle("code-425", "name-827")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
