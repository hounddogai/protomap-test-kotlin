package com.acme.transport.network.hub.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultHubEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultHubEntryStore()
        val item = newDefaultHubEntry("code-417", "label-456")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
