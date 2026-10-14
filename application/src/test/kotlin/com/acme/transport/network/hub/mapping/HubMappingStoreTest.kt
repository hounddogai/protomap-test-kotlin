package com.acme.transport.network.hub.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubMappingStore()
        val item = newHubMapping("code-448", "name-98")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
