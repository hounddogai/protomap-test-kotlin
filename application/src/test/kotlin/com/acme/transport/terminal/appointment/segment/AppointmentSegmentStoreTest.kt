package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentSegmentStore()
        val item = newAppointmentSegment("code-568", "title-431")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
