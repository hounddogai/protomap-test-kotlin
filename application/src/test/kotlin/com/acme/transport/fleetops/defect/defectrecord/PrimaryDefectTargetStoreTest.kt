package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDefectTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDefectTargetStore()
        val item = newPrimaryDefectTarget("code-967", "label-85")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
