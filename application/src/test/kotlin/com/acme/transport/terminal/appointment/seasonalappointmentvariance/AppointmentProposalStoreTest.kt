package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AppointmentProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAppointmentProposalStore()
        val item = newAppointmentProposal("code-618", "label-612")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
