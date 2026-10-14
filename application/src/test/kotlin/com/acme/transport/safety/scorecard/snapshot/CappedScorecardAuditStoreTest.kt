package com.acme.transport.safety.scorecard.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedScorecardAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedScorecardAuditStore()
        val item = newCappedScorecardAudit("code-643", "name-157")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
