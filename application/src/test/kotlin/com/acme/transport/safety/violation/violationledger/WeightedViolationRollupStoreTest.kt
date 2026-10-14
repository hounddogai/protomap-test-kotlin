package com.acme.transport.safety.violation.violationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedViolationRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedViolationRollupStore()
        val item = newWeightedViolationRollup("code-158", "name-95")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
