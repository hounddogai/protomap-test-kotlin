package com.acme.transport.safety.coaching.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingRequestStore()
        val item = newCoachingRequest("code-643", "title-208")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
