package com.acme.transport.crewing.dutyperiod.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodWindowTest {
    @Test
    fun `starts in proposed`() {
        val item = newDutyPeriodWindow("code-532", "label-736")
        assertEquals(DutyPeriodWindowStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newDutyPeriodWindow("code-532", "label-736")
        try {
            item.moveTo(DutyPeriodWindowStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodWindowStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDutyPeriodWindow("code-532", "label-736")
        item.status = DutyPeriodWindowStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DutyPeriodWindowStatus.PROPOSED) }
    }
}
