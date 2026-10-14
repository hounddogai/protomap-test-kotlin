package com.acme.transport.crewing.qualification.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class QualificationAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryQualificationAssignmentStore()
        val item = newQualificationAssignment("code-709", "title-48")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
