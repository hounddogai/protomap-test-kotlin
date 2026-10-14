package com.acme.transport.fleetops.defect.defectproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDefectAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDefectAssignmentStore()
        val item = newFallbackDefectAssignment("code-211", "name-472")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
