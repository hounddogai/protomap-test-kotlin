package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalZoneScheduleTest {
    @Test
    fun `starts in proposed`() {
        val item = newRegionalZoneSchedule("code-186", "title-488")
        assertEquals(RegionalZoneScheduleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newRegionalZoneSchedule("code-186", "title-488")
        try {
            item.moveTo(RegionalZoneScheduleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalZoneScheduleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRegionalZoneSchedule("code-186", "title-488")
        item.status = RegionalZoneScheduleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RegionalZoneScheduleStatus.PROPOSED) }
    }
}
