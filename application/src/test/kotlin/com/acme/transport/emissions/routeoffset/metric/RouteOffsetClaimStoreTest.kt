package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetClaimStore()
        val item = newRouteOffsetClaim("code-260", "label-363")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
