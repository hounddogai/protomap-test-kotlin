package com.acme.transport.linehaul.laneplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualLanePlanTargetTest {
    @Test
    fun `starts in planned`() {
        val item = newManualLanePlanTarget("code-743", "label-565")
        assertEquals(ManualLanePlanTargetStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newManualLanePlanTarget("code-743", "label-565")
        try {
            item.moveTo(ManualLanePlanTargetStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualLanePlanTargetStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newManualLanePlanTarget("code-743", "label-565")
        item.status = ManualLanePlanTargetStatus.CANCELLED
        assertFails("move") { item.moveTo(ManualLanePlanTargetStatus.PLANNED) }
    }
}
