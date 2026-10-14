package com.acme.transport.safety.scorecard.scorecardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveScorecardSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveScorecardSegmentStore()
        val item = newAdaptiveScorecardSegment("code-904", "title-718")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
