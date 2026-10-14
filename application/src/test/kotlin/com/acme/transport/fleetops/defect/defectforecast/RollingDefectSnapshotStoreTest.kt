package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDefectSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDefectSnapshotStore()
        val item = newRollingDefectSnapshot("code-831", "label-291")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
