package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubEntryStore()
        val item = newHubEntry("code-420", "name-456")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
