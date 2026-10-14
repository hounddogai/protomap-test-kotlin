package com.acme.transport.terminal.appointment.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAppointmentVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAppointmentVarianceStore()
        val item = newRegionalAppointmentVariance("code-690", "name-780")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
