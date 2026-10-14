package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentGroupStore()
        val item = newFallbackAppointmentGroup("code-543", "label-878")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
