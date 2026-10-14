package com.acme.transport.lastmile.drop.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDropSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDropSnapshotStore()
        val item = newGlobalDropSnapshot("code-225", "title-901")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
