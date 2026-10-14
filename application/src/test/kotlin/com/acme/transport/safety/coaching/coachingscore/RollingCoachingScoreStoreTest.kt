package com.acme.transport.safety.coaching.coachingscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCoachingScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCoachingScoreStore()
        val item = newRollingCoachingScore("code-908", "name-276")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
