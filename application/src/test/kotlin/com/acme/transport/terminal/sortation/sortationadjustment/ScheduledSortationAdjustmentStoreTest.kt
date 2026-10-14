package com.acme.transport.terminal.sortation.sortationadjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledSortationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledSortationAdjustmentStore()
        val item = newScheduledSortationAdjustment("code-442", "title-888")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
