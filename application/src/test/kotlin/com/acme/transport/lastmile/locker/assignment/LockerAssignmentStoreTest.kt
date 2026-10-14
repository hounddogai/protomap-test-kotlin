package com.acme.transport.lastmile.locker.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerAssignmentStore()
        val item = newLockerAssignment("code-768", "name-740")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
