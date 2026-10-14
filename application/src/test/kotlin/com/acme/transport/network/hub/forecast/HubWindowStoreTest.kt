package com.acme.transport.network.hub.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubWindowStore()
        val item = newHubWindow("code-942", "name-706")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
