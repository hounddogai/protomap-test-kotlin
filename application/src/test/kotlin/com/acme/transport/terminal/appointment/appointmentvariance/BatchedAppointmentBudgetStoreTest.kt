package com.acme.transport.terminal.appointment.appointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAppointmentBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAppointmentBudgetStore()
        val item = newBatchedAppointmentBudget("code-507", "title-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
