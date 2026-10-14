package com.acme.transport.network.transittime.timethreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackTransitTimeThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackTransitTimeThresholdStore()
        val item = newFallbackTransitTimeThreshold("code-445", "name-704")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
