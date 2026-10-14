package com.acme.transport.network.cutoff.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffEntryStore()
        val item = newCutoffEntry("code-58", "name-519")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
