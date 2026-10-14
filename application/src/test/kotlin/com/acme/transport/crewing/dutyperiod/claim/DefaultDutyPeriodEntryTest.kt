package com.acme.transport.crewing.dutyperiod.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultDutyPeriodEntryTest {
    @Test
    fun `starts in requested`() {
        val item = newDefaultDutyPeriodEntry("code-726", "label-966")
        assertEquals(DefaultDutyPeriodEntryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newDefaultDutyPeriodEntry("code-726", "label-966")
        try {
            item.moveTo(DefaultDutyPeriodEntryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultDutyPeriodEntryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newDefaultDutyPeriodEntry("code-726", "label-966")
        item.status = DefaultDutyPeriodEntryStatus.EXPIRED
        assertFails("move") { item.moveTo(DefaultDutyPeriodEntryStatus.REQUESTED) }
    }
}
