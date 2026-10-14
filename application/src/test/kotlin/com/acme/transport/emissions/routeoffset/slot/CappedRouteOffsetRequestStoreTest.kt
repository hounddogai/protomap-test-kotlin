package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRouteOffsetRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRouteOffsetRequestStore()
        val item = newCappedRouteOffsetRequest("code-907", "name-97")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
