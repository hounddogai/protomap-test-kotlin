package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedInspectionRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedInspectionRollupStore()
        val item = newWeightedInspectionRollup("code-288", "title-211")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
