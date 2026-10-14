package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentQuotaStore()
        val item = newAppointmentQuota("code-591", "title-909")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
