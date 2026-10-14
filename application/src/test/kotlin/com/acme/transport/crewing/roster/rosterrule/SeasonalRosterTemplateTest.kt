package com.acme.transport.crewing.roster.rosterrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRosterTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newSeasonalRosterTemplate("code-218", "label-14")
        assertEquals(SeasonalRosterTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newSeasonalRosterTemplate("code-218", "label-14")
        try {
            item.moveTo(SeasonalRosterTemplateStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRosterTemplateStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newSeasonalRosterTemplate("code-218", "label-14")
        item.status = SeasonalRosterTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(SeasonalRosterTemplateStatus.REQUESTED) }
    }
}
