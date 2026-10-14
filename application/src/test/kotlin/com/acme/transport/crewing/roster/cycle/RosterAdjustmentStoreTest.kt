package com.acme.transport.crewing.roster.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterAdjustmentStore()
        val item = newRosterAdjustment("code-463", "name-347")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
