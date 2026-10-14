package com.acme.transport.crewing.dutyperiod.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyDutyPeriodMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyDutyPeriodMapping("code-122", "title-10")
        assertEquals(LegacyDutyPeriodMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLegacyDutyPeriodMapping("code-122", "title-10")
        try {
            item.moveTo(LegacyDutyPeriodMappingStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyDutyPeriodMappingStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyDutyPeriodMapping("code-122", "title-10")
        item.status = LegacyDutyPeriodMappingStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyDutyPeriodMappingStatus.DRAFT) }
    }
}
