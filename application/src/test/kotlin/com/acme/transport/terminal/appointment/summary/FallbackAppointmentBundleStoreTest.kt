package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentBundleStore()
        val item = newFallbackAppointmentBundle("code-662", "title-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
