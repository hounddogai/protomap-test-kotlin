package com.acme.transport.safety.coaching.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingRollupStore()
        val item = newCoachingRollup("code-328", "title-210")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
