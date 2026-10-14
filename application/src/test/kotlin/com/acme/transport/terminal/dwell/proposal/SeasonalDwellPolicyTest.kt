package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDwellPolicyTest {
    @Test
    fun `starts in draft`() {
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        assertEquals(SeasonalDwellPolicyStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        try {
            item.moveTo(SeasonalDwellPolicyStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDwellPolicyStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newSeasonalDwellPolicy("code-494", "title-404")
        item.status = SeasonalDwellPolicyStatus.CLOSED
        assertFails("move") { item.moveTo(SeasonalDwellPolicyStatus.DRAFT) }
    }
}
