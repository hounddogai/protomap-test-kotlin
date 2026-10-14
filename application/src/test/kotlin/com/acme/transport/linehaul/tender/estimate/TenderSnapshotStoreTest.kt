package com.acme.transport.linehaul.tender.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderSnapshotStore()
        val item = newTenderSnapshot("code-215", "label-503")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
