package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentAdjustmentStore()
        val item = newAppointmentAdjustment("code-971", "label-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
