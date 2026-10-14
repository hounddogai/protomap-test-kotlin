package com.acme.transport.fleetops.defect.defectcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDefectAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDefectAuditStore()
        val item = newTieredDefectAudit("code-517", "title-155")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
