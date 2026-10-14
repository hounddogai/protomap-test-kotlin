package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedScorecardBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedScorecardBundleStore()
        val item = newBatchedScorecardBundle("code-67", "title-829")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
