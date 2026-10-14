package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLanePlanThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLanePlanThresholdStore()
        val item = newBatchedLanePlanThreshold("code-677", "name-699")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
