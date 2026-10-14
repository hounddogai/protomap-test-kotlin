package com.acme.transport.fleetops.inspection.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveInspectionAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveInspectionAuditStore()
        val item = newAdaptiveInspectionAudit("code-704", "name-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
