package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRouteOffsetDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRouteOffsetDigestStore()
        val item = newPendingRouteOffsetDigest("code-173", "title-52")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
