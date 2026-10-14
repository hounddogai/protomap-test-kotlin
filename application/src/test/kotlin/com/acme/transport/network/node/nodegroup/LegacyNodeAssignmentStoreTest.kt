package com.acme.transport.network.node.nodegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyNodeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyNodeAssignmentStore()
        val item = newLegacyNodeAssignment("code-13", "name-300")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
