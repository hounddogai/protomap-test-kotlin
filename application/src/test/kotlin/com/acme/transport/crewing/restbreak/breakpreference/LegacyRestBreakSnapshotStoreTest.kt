package com.acme.transport.crewing.restbreak.breakpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRestBreakSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRestBreakSnapshotStore()
        val item = newLegacyRestBreakSnapshot("code-680", "title-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
