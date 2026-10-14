package com.acme.transport.terminal.appointment.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAppointmentVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedAppointmentVariance("code-781", "label-952")
        assertEquals(BatchedAppointmentVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedAppointmentVariance("code-781", "label-952")
        try {
            item.moveTo(BatchedAppointmentVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAppointmentVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedAppointmentVariance("code-781", "label-952")
        item.status = BatchedAppointmentVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedAppointmentVarianceStatus.CREATED) }
    }
}
