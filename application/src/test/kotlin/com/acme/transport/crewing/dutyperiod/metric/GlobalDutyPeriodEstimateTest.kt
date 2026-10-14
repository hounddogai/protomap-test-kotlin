package com.acme.transport.crewing.dutyperiod.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalDutyPeriodEstimateTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalDutyPeriodEstimate("code-454", "title-455")
        assertEquals(GlobalDutyPeriodEstimateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newGlobalDutyPeriodEstimate("code-454", "title-455")
        try {
            item.moveTo(GlobalDutyPeriodEstimateStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalDutyPeriodEstimateStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalDutyPeriodEstimate("code-454", "title-455")
        item.status = GlobalDutyPeriodEstimateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalDutyPeriodEstimateStatus.PROPOSED) }
    }
}
