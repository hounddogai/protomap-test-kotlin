package com.acme.transport.customs.hold.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledHoldTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledHoldTemplate("code-615", "title-962")
        assertEquals(ScheduledHoldTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newScheduledHoldTemplate("code-615", "title-962")
        try {
            item.moveTo(ScheduledHoldTemplateStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledHoldTemplateStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledHoldTemplate("code-615", "title-962")
        item.status = ScheduledHoldTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledHoldTemplateStatus.PROPOSED) }
    }
}
