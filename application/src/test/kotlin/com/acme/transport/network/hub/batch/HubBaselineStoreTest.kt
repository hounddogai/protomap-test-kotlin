package com.acme.transport.network.hub.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubBaselineStore()
        val item = newHubBaseline("code-758", "title-761")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
