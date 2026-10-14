package com.acme.transport.terminal.appointment.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultAppointmentManifestTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultAppointmentManifest("code-227", "label-355")
        assertEquals(DefaultAppointmentManifestStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newDefaultAppointmentManifest("code-227", "label-355")
        try {
            item.moveTo(DefaultAppointmentManifestStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultAppointmentManifestStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultAppointmentManifest("code-227", "label-355")
        item.status = DefaultAppointmentManifestStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultAppointmentManifestStatus.REQUESTED) }
    }
}
