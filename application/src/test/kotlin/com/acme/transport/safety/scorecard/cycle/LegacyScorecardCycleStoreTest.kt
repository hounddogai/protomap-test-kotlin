package com.acme.transport.safety.scorecard.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyScorecardCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyScorecardCycleStore()
        val item = newLegacyScorecardCycle("code-754", "label-983")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
