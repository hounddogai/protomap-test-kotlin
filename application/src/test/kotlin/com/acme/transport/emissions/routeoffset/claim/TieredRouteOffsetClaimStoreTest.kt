package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRouteOffsetClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRouteOffsetClaimStore()
        val item = newTieredRouteOffsetClaim("code-764", "title-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
