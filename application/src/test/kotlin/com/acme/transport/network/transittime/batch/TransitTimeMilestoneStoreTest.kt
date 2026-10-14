package com.acme.transport.network.transittime.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TransitTimeMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTransitTimeMilestoneStore()
        val item = newTransitTimeMilestone("code-282", "label-813")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
