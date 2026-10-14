package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRestBreakRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRestBreakRollupStore()
        val item = newWeightedRestBreakRollup("code-342", "label-446")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
