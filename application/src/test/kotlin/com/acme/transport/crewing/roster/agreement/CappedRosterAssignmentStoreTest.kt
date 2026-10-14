package com.acme.transport.crewing.roster.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRosterAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRosterAssignmentStore()
        val item = newCappedRosterAssignment("code-657", "name-556")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
