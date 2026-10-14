package com.acme.transport.emissions.routeoffset.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRouteOffsetProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRouteOffsetProfileStore()
        val item = newAdaptiveRouteOffsetProfile("code-675", "title-679")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
