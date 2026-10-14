package com.acme.transport.terminal.appointment.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentScheduleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentScheduleStore()
        val item = newAppointmentSchedule("code-955", "name-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
