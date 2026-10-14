package com.acme.transport.terminal.appointment.appointmentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAppointmentForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAppointmentForecastStore()
        val item = newAdaptiveAppointmentForecast("code-780", "name-152")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
