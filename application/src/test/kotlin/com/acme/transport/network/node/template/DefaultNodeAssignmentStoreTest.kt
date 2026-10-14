package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultNodeAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultNodeAssignmentStore()
        val item = newDefaultNodeAssignment("code-499", "title-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
