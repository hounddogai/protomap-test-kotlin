package com.acme.transport.terminal.appointment.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAppointmentOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAppointmentOverrideStore()
        val item = newSeasonalAppointmentOverride("code-278", "label-174")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
