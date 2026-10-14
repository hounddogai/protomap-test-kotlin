package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualIdlingAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualIdlingAssignmentStore()
        val item = newManualIdlingAssignment("code-449", "title-798")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
