package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentAllocationStore()
        val item = newAppointmentAllocation("code-208", "title-740")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
