package com.acme.transport.network.hub.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHubMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHubMappingStore()
        val item = newCappedHubMapping("code-49", "name-225")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
