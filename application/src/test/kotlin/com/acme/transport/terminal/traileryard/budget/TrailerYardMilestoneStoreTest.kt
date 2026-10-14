package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardMilestoneStore()
        val item = newTrailerYardMilestone("code-2", "name-593")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
