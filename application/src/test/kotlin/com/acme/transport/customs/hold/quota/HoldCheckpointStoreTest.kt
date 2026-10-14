package com.acme.transport.customs.hold.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldCheckpointStore()
        val item = newHoldCheckpoint("code-720", "name-446")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
