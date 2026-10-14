package com.acme.transport.lastmile.attempt.batchedattemptrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAttemptEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAttemptEstimateStore()
        val item = newSeasonalAttemptEstimate("code-763", "title-405")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
