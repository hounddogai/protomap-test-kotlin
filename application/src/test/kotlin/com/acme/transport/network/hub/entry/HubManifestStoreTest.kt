package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubManifestStore()
        val item = newHubManifest("code-270", "label-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
