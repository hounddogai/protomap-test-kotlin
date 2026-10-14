package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceCheckpointStore()
        val item = newClearanceCheckpoint("code-451", "name-638")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
