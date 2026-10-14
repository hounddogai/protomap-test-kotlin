package com.acme.transport.crewing.dutyperiod.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveDutyPeriodRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newAdaptiveDutyPeriodRollup("code-975", "name-535")
        assertEquals(AdaptiveDutyPeriodRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newAdaptiveDutyPeriodRollup("code-975", "name-535")
        try {
            item.moveTo(AdaptiveDutyPeriodRollupStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveDutyPeriodRollupStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newAdaptiveDutyPeriodRollup("code-975", "name-535")
        item.status = AdaptiveDutyPeriodRollupStatus.STOPPED
        assertFails("move") { item.moveTo(AdaptiveDutyPeriodRollupStatus.IDLE) }
    }
}
