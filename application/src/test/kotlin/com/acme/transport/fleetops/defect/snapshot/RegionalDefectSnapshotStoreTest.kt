package com.acme.transport.fleetops.defect.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDefectSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDefectSnapshotStore()
        val item = newRegionalDefectSnapshot("code-480", "label-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
