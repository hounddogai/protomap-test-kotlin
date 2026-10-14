package com.acme.transport.safety.incident.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedIncidentMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedIncidentMilestoneStore()
        val item = newBatchedIncidentMilestone("code-29", "title-552")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
