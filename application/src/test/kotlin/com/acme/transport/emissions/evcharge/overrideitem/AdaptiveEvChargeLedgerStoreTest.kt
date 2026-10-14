package com.acme.transport.emissions.evcharge.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveEvChargeLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveEvChargeLedgerStore()
        val item = newAdaptiveEvChargeLedger("code-453", "title-789")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
