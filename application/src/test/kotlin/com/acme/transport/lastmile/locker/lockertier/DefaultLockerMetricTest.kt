package com.acme.transport.lastmile.locker.lockertier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultLockerMetricTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultLockerMetric("code-250", "label-942")
        assertEquals(DefaultLockerMetricStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newDefaultLockerMetric("code-250", "label-942")
        try {
            item.moveTo(DefaultLockerMetricStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultLockerMetricStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultLockerMetric("code-250", "label-942")
        item.status = DefaultLockerMetricStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultLockerMetricStatus.PLANNED) }
    }
}
