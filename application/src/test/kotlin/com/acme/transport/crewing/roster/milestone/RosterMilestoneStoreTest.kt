package com.acme.transport.crewing.roster.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterMilestoneStore()
        val item = newRosterMilestone("code-640", "title-383")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
