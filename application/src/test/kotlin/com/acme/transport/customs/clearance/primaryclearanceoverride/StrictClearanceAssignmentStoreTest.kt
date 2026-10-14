package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictClearanceAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictClearanceAssignmentStore()
        val item = newStrictClearanceAssignment("code-485", "label-715")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
