package com.acme.transport.customs.tariff.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffExceptionStore()
        val item = newTariffException("code-258", "title-396")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
