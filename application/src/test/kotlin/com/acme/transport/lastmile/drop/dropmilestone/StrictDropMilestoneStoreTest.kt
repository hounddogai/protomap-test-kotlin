package com.acme.transport.lastmile.drop.dropmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDropMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDropMilestoneStore()
        val item = newStrictDropMilestone("code-776", "name-332")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
