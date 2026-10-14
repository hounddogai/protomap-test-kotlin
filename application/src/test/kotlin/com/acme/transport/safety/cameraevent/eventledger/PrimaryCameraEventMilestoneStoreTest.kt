package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCameraEventMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCameraEventMilestoneStore()
        val item = newPrimaryCameraEventMilestone("code-879", "title-710")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
