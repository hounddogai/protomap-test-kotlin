package com.acme.transport.crewing.dutyperiod.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDutyPeriodRollupTest {
    @Test
    fun `starts in idle`() {
        val item = newTieredDutyPeriodRollup("code-531", "title-277")
        assertEquals(TieredDutyPeriodRollupStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to ready`() {
        val item = newTieredDutyPeriodRollup("code-531", "title-277")
        try {
            item.moveTo(TieredDutyPeriodRollupStatus.READY)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDutyPeriodRollupStatus.READY, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTieredDutyPeriodRollup("code-531", "title-277")
        item.status = TieredDutyPeriodRollupStatus.STOPPED
        assertFails("move") { item.moveTo(TieredDutyPeriodRollupStatus.IDLE) }
    }
}
