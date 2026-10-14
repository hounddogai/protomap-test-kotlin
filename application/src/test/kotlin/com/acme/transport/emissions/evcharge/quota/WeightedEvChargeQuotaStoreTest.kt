package com.acme.transport.emissions.evcharge.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedEvChargeQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedEvChargeQuotaStore()
        val item = newWeightedEvChargeQuota("code-97", "label-474")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
