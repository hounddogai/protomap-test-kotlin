package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newLanePlanThreshold("code-327", "title-909")
        assertEquals(LanePlanThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newLanePlanThreshold("code-327", "title-909")
        try {
            item.moveTo(LanePlanThresholdStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanThresholdStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newLanePlanThreshold("code-327", "title-909")
        item.status = LanePlanThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(LanePlanThresholdStatus.OPEN) }
    }
}
