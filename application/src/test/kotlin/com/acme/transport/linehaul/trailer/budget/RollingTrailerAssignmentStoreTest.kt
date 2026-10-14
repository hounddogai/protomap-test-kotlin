package com.acme.transport.linehaul.trailer.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTrailerAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTrailerAssignmentStore()
        val item = newRollingTrailerAssignment("code-714", "name-281")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
