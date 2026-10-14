package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardVarianceStore()
        val item = newScorecardVariance("code-969", "title-588")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
