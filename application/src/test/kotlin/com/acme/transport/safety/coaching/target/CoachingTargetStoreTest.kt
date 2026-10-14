package com.acme.transport.safety.coaching.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingTargetStore()
        val item = newCoachingTarget("code-336", "title-852")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
