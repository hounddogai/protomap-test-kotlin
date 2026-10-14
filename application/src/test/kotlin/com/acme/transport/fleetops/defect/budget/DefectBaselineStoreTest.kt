package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectBaselineStore()
        val item = newDefectBaseline("code-572", "label-334")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
