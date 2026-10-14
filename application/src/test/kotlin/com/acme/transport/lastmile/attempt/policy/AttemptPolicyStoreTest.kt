package com.acme.transport.lastmile.attempt.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptPolicyStore()
        val item = newAttemptPolicy("code-835", "name-797")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
