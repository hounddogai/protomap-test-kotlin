package com.acme.transport.linehaul.laneplan.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newLanePlanRecord("code-288", "title-611")
        assertEquals(LanePlanRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newLanePlanRecord("code-288", "title-611")
        try {
            item.moveTo(LanePlanRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLanePlanRecord("code-288", "title-611")
        item.status = LanePlanRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LanePlanRecordStatus.PROPOSED) }
    }
}
