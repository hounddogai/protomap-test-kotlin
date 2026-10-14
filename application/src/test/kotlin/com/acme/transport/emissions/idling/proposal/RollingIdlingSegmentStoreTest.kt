package com.acme.transport.emissions.idling.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingIdlingSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingIdlingSegmentStore()
        val item = newRollingIdlingSegment("code-190", "name-834")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
