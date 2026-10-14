package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictScorecardSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictScorecardSegmentStore()
        val item = newStrictScorecardSegment("code-222", "label-905")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
