package com.acme.transport.emissions.idling.idlingwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedIdlingThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedIdlingThresholdStore()
        val item = newBatchedIdlingThreshold("code-980", "name-808")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
