package com.acme.transport.terminal.sortation.sortationbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledSortationBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledSortationBaselineStore()
        val item = newScheduledSortationBaseline("code-763", "name-824")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
