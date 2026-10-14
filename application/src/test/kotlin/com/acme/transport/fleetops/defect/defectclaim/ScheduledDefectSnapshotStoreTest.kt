package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDefectSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDefectSnapshotStore()
        val item = newScheduledDefectSnapshot("code-954", "label-203")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
