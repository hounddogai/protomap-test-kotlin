package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentDraftStore()
        val item = newAppointmentDraft("code-10", "name-106")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
