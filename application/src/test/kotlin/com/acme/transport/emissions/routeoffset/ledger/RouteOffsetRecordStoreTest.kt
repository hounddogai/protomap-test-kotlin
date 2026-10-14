package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetRecordStore()
        val item = newRouteOffsetRecord("code-842", "name-668")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
