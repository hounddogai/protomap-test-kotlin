package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterPreferenceTest {
    @Test
    fun `starts in created`() {
        val item = newRosterPreference("code-689", "title-829")
        assertEquals(RosterPreferenceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRosterPreference("code-689", "title-829")
        try {
            item.moveTo(RosterPreferenceStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterPreferenceStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRosterPreference("code-689", "title-829")
        item.status = RosterPreferenceStatus.RETIRED
        assertFails("move") { item.moveTo(RosterPreferenceStatus.CREATED) }
    }
}
