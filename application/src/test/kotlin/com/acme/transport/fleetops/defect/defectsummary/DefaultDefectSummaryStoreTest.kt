package com.acme.transport.fleetops.defect.defectsummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDefectSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDefectSummaryStore()
        val item = newDefaultDefectSummary("code-860", "label-927")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
