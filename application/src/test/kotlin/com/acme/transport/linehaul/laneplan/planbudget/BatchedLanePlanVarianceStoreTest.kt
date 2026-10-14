package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedLanePlanVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedLanePlanVarianceStore()
        val item = newBatchedLanePlanVariance("code-582", "label-135")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
