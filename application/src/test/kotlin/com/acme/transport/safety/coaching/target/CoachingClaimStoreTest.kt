package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingClaimStore()
        val item = newCoachingClaim("code-403", "title-706")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
