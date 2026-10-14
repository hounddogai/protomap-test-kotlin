package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingTierStore()
        val item = newCoachingTier("code-276", "title-101")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
