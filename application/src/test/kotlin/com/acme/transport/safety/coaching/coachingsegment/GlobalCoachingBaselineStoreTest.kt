package com.acme.transport.safety.coaching.coachingsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingBaselineStore()
        val item = newGlobalCoachingBaseline("code-49", "title-564")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
