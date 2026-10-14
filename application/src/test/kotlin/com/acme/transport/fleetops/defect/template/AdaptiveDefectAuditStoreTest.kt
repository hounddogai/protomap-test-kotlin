package com.acme.transport.fleetops.defect.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveDefectAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveDefectAuditStore()
        val item = newAdaptiveDefectAudit("code-979", "label-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
