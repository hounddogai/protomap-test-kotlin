package com.acme.transport.network.hub.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubMilestoneStore()
        val item = newHubMilestone("code-583", "title-102")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
