package com.acme.transport.safety.coaching.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCoachingTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCoachingTierStore()
        val item = newRollingCoachingTier("code-421", "name-498")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
