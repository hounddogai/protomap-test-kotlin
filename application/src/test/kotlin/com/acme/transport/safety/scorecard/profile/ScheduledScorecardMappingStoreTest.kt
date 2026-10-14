package com.acme.transport.safety.scorecard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledScorecardMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledScorecardMappingStore()
        val item = newScheduledScorecardMapping("code-416", "name-6")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
