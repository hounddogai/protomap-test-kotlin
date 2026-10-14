package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledInspectionScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledInspectionScoreStore()
        val item = newScheduledInspectionScore("code-572", "name-691")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
