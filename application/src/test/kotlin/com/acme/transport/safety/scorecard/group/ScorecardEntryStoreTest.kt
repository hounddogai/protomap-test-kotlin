package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardEntryStore()
        val item = newScorecardEntry("code-161", "name-574")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
