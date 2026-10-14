package com.acme.transport.fleetops.inspection.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionScoreStore()
        val item = newInspectionScore("code-711", "label-392")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
