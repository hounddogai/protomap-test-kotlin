package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAppointmentRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAppointmentRollupStore()
        val item = newFallbackAppointmentRollup("code-615", "name-593")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
