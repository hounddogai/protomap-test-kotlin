package com.acme.transport.terminal.appointment.appointmenttemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAppointmentNoticeTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedAppointmentNotice("code-570", "label-193")
        assertEquals(BatchedAppointmentNoticeStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedAppointmentNotice("code-570", "label-193")
        try {
            item.moveTo(BatchedAppointmentNoticeStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAppointmentNoticeStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedAppointmentNotice("code-570", "label-193")
        item.status = BatchedAppointmentNoticeStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedAppointmentNoticeStatus.CREATED) }
    }
}
