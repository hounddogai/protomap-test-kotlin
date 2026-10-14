package com.acme.transport.terminal.appointment.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentSlotStore()
        val item = newAppointmentSlot("code-784", "title-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
