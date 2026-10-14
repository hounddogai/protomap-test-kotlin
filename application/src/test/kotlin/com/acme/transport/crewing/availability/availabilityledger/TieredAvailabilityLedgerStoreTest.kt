package com.acme.transport.crewing.availability.availabilityledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredAvailabilityLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredAvailabilityLedgerStore()
        val item = newTieredAvailabilityLedger("code-539", "label-628")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
