package com.acme.transport.customs.tariff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffDigestStore()
        val item = newTariffDigest("code-620", "title-677")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
