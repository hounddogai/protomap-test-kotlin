package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationVarianceStore()
        val item = newSortationVariance("code-651", "name-876")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
