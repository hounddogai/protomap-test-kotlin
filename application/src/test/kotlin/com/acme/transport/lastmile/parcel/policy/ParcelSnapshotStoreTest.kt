package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelSnapshotStore()
        val item = newParcelSnapshot("code-3", "label-120")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
