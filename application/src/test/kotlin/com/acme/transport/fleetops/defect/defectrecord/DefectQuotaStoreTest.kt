package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectQuotaStore()
        val item = newDefectQuota("code-926", "title-848")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
