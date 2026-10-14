package com.acme.transport.customs.inspection.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedInspectionProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedInspectionProfileStore()
        val item = newWeightedInspectionProfile("code-58", "name-558")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
