package com.acme.transport.crewing.dutyperiod.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodOverrideTest {
    @Test
    fun `starts in open`() {
        val item = newDutyPeriodOverride("code-377", "label-587")
        assertEquals(DutyPeriodOverrideStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newDutyPeriodOverride("code-377", "label-587")
        try {
            item.moveTo(DutyPeriodOverrideStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodOverrideStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDutyPeriodOverride("code-377", "label-587")
        item.status = DutyPeriodOverrideStatus.ARCHIVED
        assertFails("move") { item.moveTo(DutyPeriodOverrideStatus.OPEN) }
    }
}
