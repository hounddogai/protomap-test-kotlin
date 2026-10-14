package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedNearMissQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedNearMissQuotaStore()
        val item = newWeightedNearMissQuota("code-158", "name-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
