package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDefectThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDefectThresholdStore()
        val item = newRegionalDefectThreshold("code-976", "title-955")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
