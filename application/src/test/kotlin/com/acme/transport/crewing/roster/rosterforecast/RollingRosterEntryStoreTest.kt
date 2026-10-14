package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRosterEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRosterEntryStore()
        val item = newRollingRosterEntry("code-17", "label-175")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
