package com.acme.transport.emissions.idling.idlingmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingIdlingMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingIdlingMilestoneStore()
        val item = newPendingIdlingMilestone("code-503", "title-137")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
