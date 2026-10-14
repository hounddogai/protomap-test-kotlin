package com.acme.transport.customs.tariff.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTariffManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTariffManifestStore()
        val item = newGlobalTariffManifest("code-110", "name-491")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
