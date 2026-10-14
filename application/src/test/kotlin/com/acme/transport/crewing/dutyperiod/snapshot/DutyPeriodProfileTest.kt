package com.acme.transport.crewing.dutyperiod.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newDutyPeriodProfile("code-177", "label-965")
        assertEquals(DutyPeriodProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newDutyPeriodProfile("code-177", "label-965")
        try {
            item.moveTo(DutyPeriodProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newDutyPeriodProfile("code-177", "label-965")
        item.status = DutyPeriodProfileStatus.CLOSED
        assertFails("move") { item.moveTo(DutyPeriodProfileStatus.DRAFT) }
    }
}
