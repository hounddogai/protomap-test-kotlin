package com.acme.transport.network.hub.hubestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultHubEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultHubEstimateStore()
        val item = newDefaultHubEstimate("code-552", "label-828")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
