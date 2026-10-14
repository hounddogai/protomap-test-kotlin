package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardEstimateStore()
        val item = newScorecardEstimate("code-609", "label-691")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
