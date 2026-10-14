package com.acme.transport.fleetops.inspection.globalinspectionscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionLedgerStore()
        val item = newLegacyInspectionLedger("code-771", "title-549")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
