package com.acme.transport.lastmile.attempt.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAttemptEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAttemptEstimateStore()
        val item = newManualAttemptEstimate("code-438", "label-997")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
