package com.acme.transport.network.hub.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedHubEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedHubEntryStore()
        val item = newCappedHubEntry("code-506", "name-620")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
