package com.acme.transport.terminal.sortation.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationEstimateStore()
        val item = newSortationEstimate("code-432", "name-444")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
