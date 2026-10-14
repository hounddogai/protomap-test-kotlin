package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentLedgerStore()
        val item = newAppointmentLedger("code-773", "name-957")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
