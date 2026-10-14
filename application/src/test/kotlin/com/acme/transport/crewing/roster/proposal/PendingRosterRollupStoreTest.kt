package com.acme.transport.crewing.roster.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRosterRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRosterRollupStore()
        val item = newPendingRosterRollup("code-233", "title-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
