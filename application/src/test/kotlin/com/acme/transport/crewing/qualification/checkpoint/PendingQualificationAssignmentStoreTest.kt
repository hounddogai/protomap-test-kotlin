package com.acme.transport.crewing.qualification.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingQualificationAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingQualificationAssignmentStore()
        val item = newPendingQualificationAssignment("code-906", "label-214")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
