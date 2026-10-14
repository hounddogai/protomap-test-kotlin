package com.acme.transport.lastmile.attempt.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyAttemptCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyAttemptCycleStore()
        val item = newLegacyAttemptCycle("code-326", "title-228")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
