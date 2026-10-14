package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentSnapshotStore()
        val item = newFallbackAppointmentSnapshot("code-486", "label-93")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
