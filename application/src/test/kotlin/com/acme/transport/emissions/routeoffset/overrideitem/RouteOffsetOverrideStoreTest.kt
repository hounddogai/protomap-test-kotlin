package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetOverrideStore()
        val item = newRouteOffsetOverride("code-736", "title-93")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
