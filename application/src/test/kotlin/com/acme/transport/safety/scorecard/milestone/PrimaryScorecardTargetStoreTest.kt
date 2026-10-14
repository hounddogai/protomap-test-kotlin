package com.acme.transport.safety.scorecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryScorecardTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryScorecardTargetStore()
        val item = newPrimaryScorecardTarget("code-105", "label-659")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
