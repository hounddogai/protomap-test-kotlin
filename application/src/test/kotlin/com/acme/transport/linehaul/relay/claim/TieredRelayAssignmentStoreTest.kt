package com.acme.transport.linehaul.relay.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRelayAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRelayAssignmentStore()
        val item = newTieredRelayAssignment("code-466", "name-774")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
