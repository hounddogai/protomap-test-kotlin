package com.acme.transport.crewing.roster.rosteragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRosterTemplateTest {
    @Test
    fun `starts in requested`() {
        val item = newLegacyRosterTemplate("code-709", "title-565")
        assertEquals(LegacyRosterTemplateStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newLegacyRosterTemplate("code-709", "title-565")
        try {
            item.moveTo(LegacyRosterTemplateStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRosterTemplateStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newLegacyRosterTemplate("code-709", "title-565")
        item.status = LegacyRosterTemplateStatus.EXPIRED
        assertFails("move") { item.moveTo(LegacyRosterTemplateStatus.REQUESTED) }
    }
}
