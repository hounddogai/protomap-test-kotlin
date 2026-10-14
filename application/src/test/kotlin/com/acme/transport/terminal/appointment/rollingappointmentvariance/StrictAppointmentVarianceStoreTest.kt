package com.acme.transport.terminal.appointment.rollingappointmentvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAppointmentVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAppointmentVarianceStore()
        val item = newStrictAppointmentVariance("code-886", "name-301")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
