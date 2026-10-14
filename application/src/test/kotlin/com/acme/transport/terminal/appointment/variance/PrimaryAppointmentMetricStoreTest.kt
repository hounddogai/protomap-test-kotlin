package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryAppointmentMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryAppointmentMetricStore()
        val item = newPrimaryAppointmentMetric("code-962", "label-975")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
