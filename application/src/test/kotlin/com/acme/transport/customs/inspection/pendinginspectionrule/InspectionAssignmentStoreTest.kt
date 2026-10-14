package com.acme.transport.customs.inspection.pendinginspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionAssignmentStore()
        val item = newInspectionAssignment("code-374", "label-545")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
