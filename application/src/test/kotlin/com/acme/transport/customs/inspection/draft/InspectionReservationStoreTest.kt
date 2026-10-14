package com.acme.transport.customs.inspection.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionReservationStore()
        val item = newInspectionReservation("code-373", "label-314")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
