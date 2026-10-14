package com.acme.transport.network.cutoff.cutoffentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCutoffEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCutoffEntryStore()
        val item = newPrimaryCutoffEntry("code-367", "title-623")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
