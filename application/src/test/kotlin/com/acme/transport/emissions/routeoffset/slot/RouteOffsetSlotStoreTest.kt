package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetSlotStore()
        val item = newRouteOffsetSlot("code-358", "title-168")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
