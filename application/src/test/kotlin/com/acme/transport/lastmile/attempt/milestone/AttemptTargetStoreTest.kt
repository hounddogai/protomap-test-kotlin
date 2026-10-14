package com.acme.transport.lastmile.attempt.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptTargetStore()
        val item = newAttemptTarget("code-658", "title-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
