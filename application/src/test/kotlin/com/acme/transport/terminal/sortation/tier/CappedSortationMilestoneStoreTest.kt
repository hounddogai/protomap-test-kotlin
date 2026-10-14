package com.acme.transport.terminal.sortation.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedSortationMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedSortationMilestoneStore()
        val item = newCappedSortationMilestone("code-140", "label-421")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
