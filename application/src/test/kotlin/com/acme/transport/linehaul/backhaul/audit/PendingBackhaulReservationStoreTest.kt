package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingBackhaulReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingBackhaulReservationStore()
        val item = newPendingBackhaulReservation("code-252", "name-263")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
