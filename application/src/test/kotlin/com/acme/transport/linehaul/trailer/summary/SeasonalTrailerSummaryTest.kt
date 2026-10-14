package com.acme.transport.linehaul.trailer.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalTrailerSummaryTest {
    @Test
    fun `starts in created`() {
        val item = newSeasonalTrailerSummary("code-998", "title-446")
        assertEquals(SeasonalTrailerSummaryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newSeasonalTrailerSummary("code-998", "title-446")
        try {
            item.moveTo(SeasonalTrailerSummaryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalTrailerSummaryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newSeasonalTrailerSummary("code-998", "title-446")
        item.status = SeasonalTrailerSummaryStatus.RETIRED
        assertFails("move") { item.moveTo(SeasonalTrailerSummaryStatus.CREATED) }
    }
}
