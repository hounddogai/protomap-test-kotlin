package com.acme.transport.emissions.routeoffset.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetGroupStore()
        val item = newRouteOffsetGroup("code-914", "title-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
