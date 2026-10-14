package com.acme.transport.linehaul.relay.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayMilestoneStore()
        val item = newRelayMilestone("code-212", "name-522")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
