package com.acme.transport.crewing.availability.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityLedgerStore()
        val item = newAvailabilityLedger("code-196", "label-322")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
