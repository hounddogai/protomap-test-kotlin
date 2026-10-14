package com.acme.transport.crewing.dutyperiod.perioddraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodTierTest {
    @Test
    fun `starts in open`() {
        val item = newDutyPeriodTier("code-170", "label-368")
        assertEquals(DutyPeriodTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDutyPeriodTier("code-170", "label-368")
        try {
            item.moveTo(DutyPeriodTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDutyPeriodTier("code-170", "label-368")
        item.status = DutyPeriodTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(DutyPeriodTierStatus.OPEN) }
    }
}
