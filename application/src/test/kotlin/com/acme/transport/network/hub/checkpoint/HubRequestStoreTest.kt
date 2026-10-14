package com.acme.transport.network.hub.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubRequestStore()
        val item = newHubRequest("code-675", "label-434")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
