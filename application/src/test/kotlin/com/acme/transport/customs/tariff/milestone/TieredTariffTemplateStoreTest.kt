package com.acme.transport.customs.tariff.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTariffTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTariffTemplateStore()
        val item = newTieredTariffTemplate("code-134", "title-510")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
