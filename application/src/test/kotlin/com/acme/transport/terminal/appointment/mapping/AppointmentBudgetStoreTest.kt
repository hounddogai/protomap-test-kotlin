package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentBudgetStore()
        val item = newAppointmentBudget("code-358", "title-880")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
