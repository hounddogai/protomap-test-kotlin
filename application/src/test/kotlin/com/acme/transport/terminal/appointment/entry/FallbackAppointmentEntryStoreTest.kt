package com.acme.transport.terminal.appointment.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentEntryStore()
        val item = newFallbackAppointmentEntry("code-280", "title-264")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
