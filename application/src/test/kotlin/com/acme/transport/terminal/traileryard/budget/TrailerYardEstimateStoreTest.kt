package com.acme.transport.terminal.traileryard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardEstimateStore()
        val item = newTrailerYardEstimate("code-862", "label-903")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
