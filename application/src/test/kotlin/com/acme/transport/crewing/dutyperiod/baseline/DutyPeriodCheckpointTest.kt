package com.acme.transport.crewing.dutyperiod.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DutyPeriodCheckpointTest {
    @Test
    fun `starts in created`() {
        val item = newDutyPeriodCheckpoint("code-483", "label-870")
        assertEquals(DutyPeriodCheckpointStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newDutyPeriodCheckpoint("code-483", "label-870")
        try {
            item.moveTo(DutyPeriodCheckpointStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DutyPeriodCheckpointStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newDutyPeriodCheckpoint("code-483", "label-870")
        item.status = DutyPeriodCheckpointStatus.RETIRED
        assertFails("move") { item.moveTo(DutyPeriodCheckpointStatus.CREATED) }
    }
}
