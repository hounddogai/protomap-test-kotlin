package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRouteOffsetProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRouteOffsetProfileStore()
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
