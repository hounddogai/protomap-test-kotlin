package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientViolationAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientViolationAssignmentStore()
        val item = newLenientViolationAssignment("code-459", "label-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
