package com.acme.transport.terminal.appointment.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAppointmentDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAppointmentDigestStore()
        val item = newSeasonalAppointmentDigest("code-492", "name-297")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
