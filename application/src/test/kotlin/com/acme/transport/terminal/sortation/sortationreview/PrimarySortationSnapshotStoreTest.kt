package com.acme.transport.terminal.sortation.sortationreview

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimarySortationSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimarySortationSnapshotStore()
        val item = newPrimarySortationSnapshot("code-285", "label-767")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
