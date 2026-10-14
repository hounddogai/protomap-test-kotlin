package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedScorecardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedScorecardRollupStore()
        val item = newBatchedScorecardRollup("code-296", "label-963")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
