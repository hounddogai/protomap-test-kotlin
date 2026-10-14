package com.acme.transport.terminal.appointment.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAppointmentSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAppointmentSnapshotStore()
        val item = newPrimaryAppointmentSnapshot("code-625", "label-414")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
