package com.acme.transport.terminal.sortation.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedSortationBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedSortationBundleStore()
        val item = newWeightedSortationBundle("code-22", "name-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
