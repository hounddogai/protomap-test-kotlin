package com.acme.transport.crewing.dutyperiod.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientDutyPeriodProfileTest {
    @Test
    fun `starts in draft`() {
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        assertEquals(LenientDutyPeriodProfileStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        try {
            item.moveTo(LenientDutyPeriodProfileStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientDutyPeriodProfileStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLenientDutyPeriodProfile("code-500", "name-862")
        item.status = LenientDutyPeriodProfileStatus.CLOSED
        assertFails("move") { item.moveTo(LenientDutyPeriodProfileStatus.DRAFT) }
    }
}
