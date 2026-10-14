package com.acme.transport.lastmile.attempt.attemptplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAttemptPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAttemptPlanStore()
        val item = newStrictAttemptPlan("code-312", "title-159")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
