package com.acme.transport.customs.tariff.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTariffQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTariffQuotaStore()
        val item = newManualTariffQuota("code-974", "title-474")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
