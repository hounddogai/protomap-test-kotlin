package com.acme.transport.terminal.appointment.appointmentallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAppointmentScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAppointmentScoreStore()
        val item = newPendingAppointmentScore("code-344", "name-153")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
