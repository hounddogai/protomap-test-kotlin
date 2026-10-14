package com.acme.transport.crewing.roster.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RosterScoreTest {
    @Test
    fun `starts in new`() {
        val item = newRosterScore("code-786", "label-248")
        assertEquals(RosterScoreStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newRosterScore("code-786", "label-248")
        try {
            item.moveTo(RosterScoreStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RosterScoreStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRosterScore("code-786", "label-248")
        item.status = RosterScoreStatus.FAILED
        assertFails("move") { item.moveTo(RosterScoreStatus.NEW) }
    }
}
