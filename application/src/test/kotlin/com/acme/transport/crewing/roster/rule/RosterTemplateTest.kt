package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterTemplateTest {
    @Test
    fun `starts in new`() {
        val item = newRosterTemplate("code-926", "name-186")
        assertEquals(RosterTemplateStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newRosterTemplate("code-926", "name-186")
        try {
            item.moveTo(RosterTemplateStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterTemplateStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRosterTemplate("code-926", "name-186")
        item.status = RosterTemplateStatus.FAILED
        assertFails("move") { item.moveTo(RosterTemplateStatus.NEW) }
    }
}
