package com.acme.transport.linehaul.relay.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveRelayAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveRelayAssignmentStore()
        val item = newAdaptiveRelayAssignment("code-442", "title-579")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
