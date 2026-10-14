package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newSeasonalDeclarationSummary("code-983", "title-737")
        assertEquals(SeasonalDeclarationSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newSeasonalDeclarationSummary("code-983", "title-737")
        try {
            item.moveTo(SeasonalDeclarationSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newSeasonalDeclarationSummary("code-983", "title-737")
        item.status = SeasonalDeclarationSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(SeasonalDeclarationSummaryStatus.PROPOSED) }
    }
}
