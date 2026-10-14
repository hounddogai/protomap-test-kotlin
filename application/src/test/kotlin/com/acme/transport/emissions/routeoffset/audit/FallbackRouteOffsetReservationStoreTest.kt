package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRouteOffsetReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRouteOffsetReservationStore()
        val item = newFallbackRouteOffsetReservation("code-83", "title-666")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
