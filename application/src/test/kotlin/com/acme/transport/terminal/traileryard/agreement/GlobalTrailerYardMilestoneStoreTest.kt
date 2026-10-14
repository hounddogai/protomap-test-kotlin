package com.acme.transport.terminal.traileryard.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerYardMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerYardMilestoneStore()
        val item = newGlobalTrailerYardMilestone("code-825", "title-543")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
