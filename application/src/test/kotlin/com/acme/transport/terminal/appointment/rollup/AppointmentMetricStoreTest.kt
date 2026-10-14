package com.acme.transport.terminal.appointment.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentMetricStore()
        val item = newAppointmentMetric("code-519", "label-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
