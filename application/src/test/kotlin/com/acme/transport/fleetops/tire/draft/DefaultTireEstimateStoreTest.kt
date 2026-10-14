package com.acme.transport.fleetops.tire.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTireEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTireEstimateStore()
        val item = newDefaultTireEstimate("code-888", "name-965")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
