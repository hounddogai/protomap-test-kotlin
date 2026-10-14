package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingMappingStore()
        val item = newCoachingMapping("code-986", "title-211")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
