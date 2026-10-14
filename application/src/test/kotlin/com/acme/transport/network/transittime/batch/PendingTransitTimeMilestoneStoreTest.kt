package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTransitTimeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTransitTimeMilestoneStore()
        val item = newPendingTransitTimeMilestone("code-850", "name-116")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
