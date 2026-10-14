package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRelayMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRelayMilestoneStore()
        val item = newSeasonalRelayMilestone("code-92", "name-752")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
