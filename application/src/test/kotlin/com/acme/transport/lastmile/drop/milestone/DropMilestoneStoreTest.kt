package com.acme.transport.lastmile.drop.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropMilestoneStore()
        val item = newDropMilestone("code-131", "name-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
