package com.acme.transport.network.hub.primaryhubsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedHubTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedHubTierStore()
        val item = newBatchedHubTier("code-292", "title-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
