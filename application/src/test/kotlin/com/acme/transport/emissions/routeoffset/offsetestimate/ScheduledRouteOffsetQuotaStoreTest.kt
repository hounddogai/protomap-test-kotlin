package com.acme.transport.emissions.routeoffset.offsetestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRouteOffsetQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRouteOffsetQuotaStore()
        val item = newScheduledRouteOffsetQuota("code-292", "title-502")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
