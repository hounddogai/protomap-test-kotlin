package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingNearMissSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingNearMissSlotStore()
        val item = newPendingNearMissSlot("code-635", "name-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
