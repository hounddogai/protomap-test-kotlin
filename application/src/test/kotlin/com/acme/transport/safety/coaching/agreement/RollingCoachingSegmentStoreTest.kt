package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCoachingSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCoachingSegmentStore()
        val item = newRollingCoachingSegment("code-791", "title-802")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
