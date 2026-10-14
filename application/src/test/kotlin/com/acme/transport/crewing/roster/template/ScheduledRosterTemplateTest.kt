package com.acme.transport.crewing.roster.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScheduledRosterTemplateTest {
    @Test
    fun `starts in proposed`() {
        val item = newScheduledRosterTemplate("code-628", "name-830")
        assertEquals(ScheduledRosterTemplateStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newScheduledRosterTemplate("code-628", "name-830")
        try {
            item.moveTo(ScheduledRosterTemplateStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScheduledRosterTemplateStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newScheduledRosterTemplate("code-628", "name-830")
        item.status = ScheduledRosterTemplateStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ScheduledRosterTemplateStatus.PROPOSED) }
    }
}
