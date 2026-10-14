package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryNodeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryNodeMilestoneStore()
        val item = newPrimaryNodeMilestone("code-282", "name-352")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
