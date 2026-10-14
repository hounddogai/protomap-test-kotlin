package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingPolicyStore()
        val item = newCoachingPolicy("code-455", "name-837")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
