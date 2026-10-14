package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRouteOffsetLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRouteOffsetLedgerStore()
        val item = newDefaultRouteOffsetLedger("code-427", "name-915")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
