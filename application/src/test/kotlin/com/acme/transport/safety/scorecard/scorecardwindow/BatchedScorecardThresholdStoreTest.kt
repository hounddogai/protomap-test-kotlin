package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedScorecardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedScorecardThresholdStore()
        val item = newBatchedScorecardThreshold("code-987", "name-96")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
