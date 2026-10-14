package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTireThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTireThresholdStore()
        val item = newBatchedTireThreshold("code-156", "title-930")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
