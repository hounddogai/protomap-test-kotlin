package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRouteOffsetProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRouteOffsetProfileStore()
        val item = newPendingRouteOffsetProfile("code-864", "title-763")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
