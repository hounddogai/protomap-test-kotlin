package com.acme.transport.lastmile.locker.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerMetricTest {
    @Test
    fun `starts in draft`() {
        val item = newLockerMetric("code-182", "title-692")
        assertEquals(LockerMetricStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLockerMetric("code-182", "title-692")
        try {
            item.moveTo(LockerMetricStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerMetricStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLockerMetric("code-182", "title-692")
        item.status = LockerMetricStatus.CLOSED
        assertFails("move") { item.moveTo(LockerMetricStatus.DRAFT) }
    }
}
