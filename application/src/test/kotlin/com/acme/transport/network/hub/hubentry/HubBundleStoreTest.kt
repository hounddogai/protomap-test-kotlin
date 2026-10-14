package com.acme.transport.network.hub.hubentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubBundleStore()
        val item = newHubBundle("code-195", "label-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
