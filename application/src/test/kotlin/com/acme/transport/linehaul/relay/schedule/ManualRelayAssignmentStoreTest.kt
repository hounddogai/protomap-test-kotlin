package com.acme.transport.linehaul.relay.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRelayAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRelayAssignmentStore()
        val item = newManualRelayAssignment("code-9", "title-658")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
