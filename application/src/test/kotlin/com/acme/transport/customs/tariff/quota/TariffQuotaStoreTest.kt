package com.acme.transport.customs.tariff.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffQuotaStore()
        val item = newTariffQuota("code-170", "name-118")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
