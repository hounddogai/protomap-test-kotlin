package com.acme.transport.fleetops.defect.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDefectMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDefectMilestoneStore()
        val item = newManualDefectMilestone("code-532", "name-728")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
