package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationTargetTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalDeclarationTarget("code-981", "title-181")
        assertEquals(SeasonalDeclarationTargetStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newSeasonalDeclarationTarget("code-981", "title-181")
        try {
            item.moveTo(SeasonalDeclarationTargetStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationTargetStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalDeclarationTarget("code-981", "title-181")
        item.status = SeasonalDeclarationTargetStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalDeclarationTargetStatus.PROPOSED) }
    }
}
