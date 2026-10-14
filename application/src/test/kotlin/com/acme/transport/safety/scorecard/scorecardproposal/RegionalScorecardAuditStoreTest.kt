package com.acme.transport.safety.scorecard.scorecardproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalScorecardAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalScorecardAuditStore()
        val item = newRegionalScorecardAudit("code-129", "name-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
