package com.acme.transport.terminal.appointment.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAppointmentMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAppointmentMetricStore()
        val item = newWeightedAppointmentMetric("code-422", "name-929")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
