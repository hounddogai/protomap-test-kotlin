package com.acme.transport.safety.scorecard.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackScorecardTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackScorecardTargetStore()
        val item = newFallbackScorecardTarget("code-875", "name-251")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
