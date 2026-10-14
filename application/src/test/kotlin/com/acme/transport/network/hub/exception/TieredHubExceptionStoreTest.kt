package com.acme.transport.network.hub.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredHubExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredHubExceptionStore()
        val item = newTieredHubException("code-123", "name-971")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
