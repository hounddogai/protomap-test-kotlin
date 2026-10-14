package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodDigestTest {
    @Test
    fun `starts in idle`() {
        val item = newDutyPeriodDigest("code-325", "title-471")
        assertEquals(DutyPeriodDigestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newDutyPeriodDigest("code-325", "title-471")
        try {
            item.moveTo(DutyPeriodDigestStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodDigestStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newDutyPeriodDigest("code-325", "title-471")
        item.status = DutyPeriodDigestStatus.STOPPED
        assertFails("move") { item.moveTo(DutyPeriodDigestStatus.IDLE) }
    }
}
