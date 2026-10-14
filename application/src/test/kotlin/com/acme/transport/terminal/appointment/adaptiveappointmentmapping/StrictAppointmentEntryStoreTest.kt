package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAppointmentEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAppointmentEntryStore()
        val item = newStrictAppointmentEntry("code-160", "label-479")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
