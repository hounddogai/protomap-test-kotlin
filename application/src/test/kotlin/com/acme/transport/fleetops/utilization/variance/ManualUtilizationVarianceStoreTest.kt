package com.acme.transport.fleetops.utilization.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualUtilizationVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualUtilizationVarianceStore()
        val item = newManualUtilizationVariance("code-554", "label-245")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
