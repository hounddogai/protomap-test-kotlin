package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptRuleStore()
        val item = newAttemptRule("code-513", "label-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
