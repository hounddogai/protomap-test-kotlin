package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptCycleStore()
        val item = newAttemptCycle("code-766", "name-125")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
