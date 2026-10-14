package com.acme.transport.crewing.restbreak.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRestBreakSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRestBreakSnapshotStore()
        val item = newGlobalRestBreakSnapshot("code-917", "title-290")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
