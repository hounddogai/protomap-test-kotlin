package com.acme.transport.crewing.roster.rosterquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterCheckpointStore()
        val item = newRosterCheckpoint("code-127", "name-664")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
