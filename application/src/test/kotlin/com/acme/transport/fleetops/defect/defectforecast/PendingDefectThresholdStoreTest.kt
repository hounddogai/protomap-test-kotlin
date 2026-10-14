package com.acme.transport.fleetops.defect.defectforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDefectThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDefectThresholdStore()
        val item = newPendingDefectThreshold("code-481", "label-723")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
