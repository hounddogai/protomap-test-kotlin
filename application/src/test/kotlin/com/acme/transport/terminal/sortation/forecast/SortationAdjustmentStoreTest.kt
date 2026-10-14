package com.acme.transport.terminal.sortation.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationAdjustmentStore()
        val item = newSortationAdjustment("code-808", "label-564")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
