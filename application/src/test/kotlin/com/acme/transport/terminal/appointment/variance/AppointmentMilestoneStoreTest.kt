package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentMilestoneStore()
        val item = newAppointmentMilestone("code-83", "label-918")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
