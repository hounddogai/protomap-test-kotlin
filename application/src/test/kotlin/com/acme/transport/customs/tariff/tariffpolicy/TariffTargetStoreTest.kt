package com.acme.transport.customs.tariff.tariffpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffTargetStore()
        val item = newTariffTarget("code-598", "title-443")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
