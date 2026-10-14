package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentGroupStore()
        val item = newAppointmentGroup("code-592", "label-242")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
