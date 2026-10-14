package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentBaselineStore()
        val item = newFallbackAppointmentBaseline("code-290", "title-521")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
