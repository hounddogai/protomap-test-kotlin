package com.acme.transport.safety.scorecard.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ScorecardTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newScorecardTemplate("code-239", "title-998")
        assertEquals(ScorecardTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newScorecardTemplate("code-239", "title-998")
        try {
            item.moveTo(ScorecardTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ScorecardTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newScorecardTemplate("code-239", "title-998")
        item.status = ScorecardTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(ScorecardTemplateStatus.CREATED) }
    }
}
