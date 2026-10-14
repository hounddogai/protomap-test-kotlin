package com.acme.transport.customs.clearance.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceSnapshotStore()
        val item = newClearanceSnapshot("code-432", "title-318")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
