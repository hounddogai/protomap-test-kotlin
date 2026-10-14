package com.acme.transport.emissions.routeoffset.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRouteOffsetProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRouteOffsetProfileStore()
        val item = newManualRouteOffsetProfile("code-447", "name-464")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
