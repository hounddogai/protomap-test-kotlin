package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceScheduleStore()
        val item = newClearanceSchedule("code-984", "name-242")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
