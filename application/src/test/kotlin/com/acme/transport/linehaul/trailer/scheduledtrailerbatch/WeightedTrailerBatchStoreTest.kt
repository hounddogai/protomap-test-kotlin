package com.acme.transport.linehaul.trailer.scheduledtrailerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedTrailerBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedTrailerBatchStore()
        val item = newWeightedTrailerBatch("code-404", "title-121")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
