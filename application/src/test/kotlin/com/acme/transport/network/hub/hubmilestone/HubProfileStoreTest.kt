package com.acme.transport.network.hub.hubmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubProfileStore()
        val item = newHubProfile("code-24", "label-53")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
