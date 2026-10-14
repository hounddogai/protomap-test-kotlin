package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualDefectTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualDefectTargetStore()
        val item = newManualDefectTarget("code-967", "label-69")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
