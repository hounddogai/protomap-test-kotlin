package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTenderSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTenderSnapshotStore()
        val item = newPendingTenderSnapshot("code-260", "label-375")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
