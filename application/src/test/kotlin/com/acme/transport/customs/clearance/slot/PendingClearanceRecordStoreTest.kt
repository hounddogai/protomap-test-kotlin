package com.acme.transport.customs.clearance.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingClearanceRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingClearanceRecordStore()
        val item = newPendingClearanceRecord("code-334", "name-51")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
