package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetReviewStore()
        val item = newRouteOffsetReview("code-201", "name-363")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
