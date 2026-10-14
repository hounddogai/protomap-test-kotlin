package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardOverrideStore()
        val item = newScorecardOverride("code-476", "title-814")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
