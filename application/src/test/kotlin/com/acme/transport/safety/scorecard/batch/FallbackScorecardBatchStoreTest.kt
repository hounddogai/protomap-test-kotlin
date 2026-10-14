package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackScorecardBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackScorecardBatchStore()
        val item = newFallbackScorecardBatch("code-356", "label-53")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
