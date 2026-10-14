package com.acme.transport.safety.scorecard.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedScorecardLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedScorecardLedgerStore()
        val item = newBatchedScorecardLedger("code-972", "name-917")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
