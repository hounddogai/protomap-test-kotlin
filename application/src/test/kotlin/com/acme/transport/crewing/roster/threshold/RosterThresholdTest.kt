package com.acme.transport.crewing.roster.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newRosterThreshold("code-635", "title-76")
        assertEquals(RosterThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRosterThreshold("code-635", "title-76")
        try {
            item.moveTo(RosterThresholdStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterThresholdStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterThreshold("code-635", "title-76")
        item.status = RosterThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(RosterThresholdStatus.CREATED) }
    }
}
