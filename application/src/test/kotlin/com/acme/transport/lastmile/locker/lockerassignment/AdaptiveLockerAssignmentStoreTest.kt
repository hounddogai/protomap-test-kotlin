package com.acme.transport.lastmile.locker.lockerassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLockerAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLockerAssignmentStore()
        val item = newAdaptiveLockerAssignment("code-19", "name-939")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
