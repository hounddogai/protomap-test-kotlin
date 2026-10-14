package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetNoticeStore()
        val item = newRouteOffsetNotice("code-500", "label-475")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
