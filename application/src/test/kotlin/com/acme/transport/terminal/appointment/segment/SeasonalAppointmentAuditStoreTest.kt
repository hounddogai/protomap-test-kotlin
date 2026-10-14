package com.acme.transport.terminal.appointment.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAppointmentAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAppointmentAuditStore()
        val item = newSeasonalAppointmentAudit("code-485", "label-108")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
