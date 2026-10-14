package com.acme.transport.emissions.routeoffset.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRouteOffsetNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRouteOffsetNoticeStore()
        val item = newFallbackRouteOffsetNotice("code-885", "name-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
