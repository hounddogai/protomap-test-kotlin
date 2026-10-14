package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCoachingWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCoachingWindowStore()
        val item = newLegacyCoachingWindow("code-919", "label-816")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
