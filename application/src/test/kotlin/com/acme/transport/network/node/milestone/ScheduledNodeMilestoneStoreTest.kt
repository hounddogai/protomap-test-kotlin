package com.acme.transport.network.node.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledNodeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledNodeMilestoneStore()
        val item = newScheduledNodeMilestone("code-260", "title-40")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
