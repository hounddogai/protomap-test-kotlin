package com.acme.transport.terminal.traileryard.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerYardEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerYardEstimateStore()
        val item = newManualTrailerYardEstimate("code-684", "name-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
