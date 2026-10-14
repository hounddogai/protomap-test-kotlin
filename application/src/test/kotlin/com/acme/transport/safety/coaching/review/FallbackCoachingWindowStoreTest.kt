package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCoachingWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCoachingWindowStore()
        val item = newFallbackCoachingWindow("code-436", "title-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
