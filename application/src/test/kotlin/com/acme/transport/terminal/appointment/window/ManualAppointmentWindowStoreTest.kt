package com.acme.transport.terminal.appointment.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAppointmentWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAppointmentWindowStore()
        val item = newManualAppointmentWindow("code-374", "label-362")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
