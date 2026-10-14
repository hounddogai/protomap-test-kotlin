package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAppointmentForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAppointmentForecastStore()
        val item = newScheduledAppointmentForecast("code-716", "title-776")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
