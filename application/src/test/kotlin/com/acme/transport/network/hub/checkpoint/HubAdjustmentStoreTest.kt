package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubAdjustmentStore()
        val item = newHubAdjustment("code-615", "name-217")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
