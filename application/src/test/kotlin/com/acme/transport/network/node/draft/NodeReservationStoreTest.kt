package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodeReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodeReservationStore()
        val item = newNodeReservation("code-741", "title-856")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
