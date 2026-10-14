package com.acme.transport.emissions.routeoffset.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetProposalStore()
        val item = newRouteOffsetProposal("code-433", "title-530")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
