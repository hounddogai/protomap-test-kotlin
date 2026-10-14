package com.acme.transport.network.servicearea.areabatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalServiceAreaEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalServiceAreaEstimateStore()
        val item = newRegionalServiceAreaEstimate("code-347", "label-300")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
