package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedTariffDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedTariffDigestStore()
        val item = newCappedTariffDigest("code-555", "title-871")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
