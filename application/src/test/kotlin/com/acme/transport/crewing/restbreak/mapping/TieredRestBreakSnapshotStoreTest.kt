package com.acme.transport.crewing.restbreak.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRestBreakSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRestBreakSnapshotStore()
        val item = newTieredRestBreakSnapshot("code-830", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
