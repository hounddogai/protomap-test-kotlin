package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardBatchStore()
        val item = newScorecardBatch("code-352", "title-767")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
