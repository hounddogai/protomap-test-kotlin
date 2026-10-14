package com.acme.transport.emissions.idling.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultIdlingEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultIdlingEntryStore()
        val item = newDefaultIdlingEntry("code-825", "name-700")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
