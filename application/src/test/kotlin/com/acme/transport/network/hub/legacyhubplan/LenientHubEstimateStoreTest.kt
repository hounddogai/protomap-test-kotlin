package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientHubEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientHubEstimateStore()
        val item = newLenientHubEstimate("code-140", "name-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
