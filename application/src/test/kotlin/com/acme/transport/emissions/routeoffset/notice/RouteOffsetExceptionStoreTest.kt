package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetExceptionStore()
        val item = newRouteOffsetException("code-614", "title-402")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
