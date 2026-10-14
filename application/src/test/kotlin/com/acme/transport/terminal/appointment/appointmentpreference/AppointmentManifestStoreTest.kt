package com.acme.transport.terminal.appointment.appointmentpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentManifestStore()
        val item = newAppointmentManifest("code-558", "name-381")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
