package com.acme.transport.safety.coaching.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingSummaryStore()
        val item = newCoachingSummary("code-840", "name-158")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
