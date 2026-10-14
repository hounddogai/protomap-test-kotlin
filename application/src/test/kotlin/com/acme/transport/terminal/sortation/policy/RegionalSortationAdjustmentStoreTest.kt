package com.acme.transport.terminal.sortation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalSortationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalSortationAdjustmentStore()
        val item = newRegionalSortationAdjustment("code-166", "title-816")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
