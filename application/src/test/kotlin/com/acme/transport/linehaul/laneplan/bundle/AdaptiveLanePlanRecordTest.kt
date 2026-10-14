package com.acme.transport.linehaul.laneplan.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveLanePlanRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newAdaptiveLanePlanRecord("code-218", "name-740")
        assertEquals(AdaptiveLanePlanRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newAdaptiveLanePlanRecord("code-218", "name-740")
        try {
            item.moveTo(AdaptiveLanePlanRecordStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveLanePlanRecordStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newAdaptiveLanePlanRecord("code-218", "name-740")
        item.status = AdaptiveLanePlanRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(AdaptiveLanePlanRecordStatus.PROPOSED) }
    }
}
