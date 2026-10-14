package com.acme.transport.terminal.appointment.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentWindowStore()
        val item = newAppointmentWindow("code-790", "title-30")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
