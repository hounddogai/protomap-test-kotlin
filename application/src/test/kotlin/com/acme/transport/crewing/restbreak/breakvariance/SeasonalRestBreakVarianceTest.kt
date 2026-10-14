package com.acme.transport.crewing.restbreak.breakvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRestBreakVarianceTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalRestBreakVariance("code-581", "title-983")
        assertEquals(SeasonalRestBreakVarianceStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newSeasonalRestBreakVariance("code-581", "title-983")
        try {
            item.moveTo(SeasonalRestBreakVarianceStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRestBreakVarianceStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalRestBreakVariance("code-581", "title-983")
        item.status = SeasonalRestBreakVarianceStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalRestBreakVarianceStatus.CREATED) }
    }
}
