package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptAssignmentStore()
        val item = newAttemptAssignment("code-154", "title-509")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
