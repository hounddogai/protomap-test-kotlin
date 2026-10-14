package com.acme.transport.fleetops.defect.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDefectSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDefectSnapshotStore()
        val item = newGlobalDefectSnapshot("code-957", "name-158")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
