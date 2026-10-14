package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAppointmentManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAppointmentManifestStore()
        val item = newDefaultAppointmentManifest("code-227", "label-355")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
