package com.acme.transport.terminal.appointment.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAppointmentBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAppointmentBaselineStore()
        val item = newPendingAppointmentBaseline("code-11", "name-402")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
