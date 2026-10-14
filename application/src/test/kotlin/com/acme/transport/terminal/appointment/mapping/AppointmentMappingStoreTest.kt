package com.acme.transport.terminal.appointment.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentMappingStore()
        val item = newAppointmentMapping("code-525", "title-647")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
