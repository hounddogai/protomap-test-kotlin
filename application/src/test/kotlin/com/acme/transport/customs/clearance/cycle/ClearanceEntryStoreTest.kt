package com.acme.transport.customs.clearance.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceEntryStore()
        val item = newClearanceEntry("code-679", "name-126")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
