package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LenientRosterRuleTest {
    @Test
    fun `starts in idle`() {
        val item = newLenientRosterRule("code-248", "title-281")
        assertEquals(LenientRosterRuleStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newLenientRosterRule("code-248", "title-281")
        try {
            item.moveTo(LenientRosterRuleStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LenientRosterRuleStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newLenientRosterRule("code-248", "title-281")
        item.status = LenientRosterRuleStatus.STOPPED
        assertFails("move") { item.moveTo(LenientRosterRuleStatus.IDLE) }
    }
}
