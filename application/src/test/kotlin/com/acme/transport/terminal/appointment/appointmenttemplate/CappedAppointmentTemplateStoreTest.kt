package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedAppointmentTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedAppointmentTemplateStore()
        val item = newCappedAppointmentTemplate("code-124", "label-129")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
