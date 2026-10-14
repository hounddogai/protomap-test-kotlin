package com.acme.transport.terminal.appointment.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackAppointmentBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackAppointmentBundle("code-662", "title-204")
        val view = toFallbackAppointmentBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackAppointmentBundleViews(mutableListOf<FallbackAppointmentBundle>(item, item)).size, "views")
    }
}
