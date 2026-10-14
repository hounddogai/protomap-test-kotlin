package com.acme.transport.emissions.routeoffset.offsetmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRouteOffsetQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRouteOffsetQuotaStore()
        val item = newWeightedRouteOffsetQuota("code-346", "label-296")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
