package com.acme.transport.terminal.appointment.appointmentscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAppointmentScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAppointmentScoreStore()
        val item = newBatchedAppointmentScore("code-559", "title-107")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
