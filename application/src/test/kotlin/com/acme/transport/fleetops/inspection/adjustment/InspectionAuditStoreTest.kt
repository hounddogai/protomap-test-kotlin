package com.acme.transport.fleetops.inspection.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionAuditStore()
        val item = newInspectionAudit("code-209", "title-201")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
