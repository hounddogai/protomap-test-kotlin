package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAppointmentEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAppointmentEstimateStore()
        val item = newScheduledAppointmentEstimate("code-524", "name-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
