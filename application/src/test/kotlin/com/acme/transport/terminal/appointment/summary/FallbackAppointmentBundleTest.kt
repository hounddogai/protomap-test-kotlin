package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackAppointmentBundleTest {
    @Test
    fun `starts in proposed`() {
        val item = newFallbackAppointmentBundle("code-662", "title-204")
        assertEquals(FallbackAppointmentBundleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newFallbackAppointmentBundle("code-662", "title-204")
        try {
            item.moveTo(FallbackAppointmentBundleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackAppointmentBundleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newFallbackAppointmentBundle("code-662", "title-204")
        item.status = FallbackAppointmentBundleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(FallbackAppointmentBundleStatus.PROPOSED) }
    }
}
