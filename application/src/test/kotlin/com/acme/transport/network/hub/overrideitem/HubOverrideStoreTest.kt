package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubOverrideStore()
        val item = newHubOverride("code-972", "title-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
