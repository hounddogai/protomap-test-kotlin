package com.acme.transport.emissions.routeoffset.offsetnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRouteOffsetNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRouteOffsetNoticeStore()
        val item = newSeasonalRouteOffsetNotice("code-462", "name-357")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
