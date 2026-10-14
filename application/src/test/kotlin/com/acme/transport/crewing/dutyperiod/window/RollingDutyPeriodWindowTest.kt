package com.acme.transport.crewing.dutyperiod.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingDutyPeriodWindowTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingDutyPeriodWindow("code-541", "name-629")
        assertEquals(RollingDutyPeriodWindowStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newRollingDutyPeriodWindow("code-541", "name-629")
        try {
            item.moveTo(RollingDutyPeriodWindowStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingDutyPeriodWindowStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingDutyPeriodWindow("code-541", "name-629")
        item.status = RollingDutyPeriodWindowStatus.CLOSED
        assertFails("move") { item.moveTo(RollingDutyPeriodWindowStatus.DRAFT) }
    }
}
