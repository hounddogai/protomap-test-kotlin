package com.acme.transport.customs.inspection.inspectionplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionReservationStore()
        val item = newPrimaryInspectionReservation("code-182", "label-364")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
