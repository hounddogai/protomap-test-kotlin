package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyCoachingSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyCoachingSnapshotStore()
        val item = newLegacyCoachingSnapshot("code-623", "title-427")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
