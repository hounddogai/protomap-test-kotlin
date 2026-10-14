package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDwellAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDwellAllocationStore()
        val item = newScheduledDwellAllocation("code-129", "title-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
