package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newRestBreakForecast("code-640", "title-637")
        assertEquals(RestBreakForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newRestBreakForecast("code-640", "title-637")
        try {
            item.moveTo(RestBreakForecastStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakForecastStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRestBreakForecast("code-640", "title-637")
        item.status = RestBreakForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RestBreakForecastStatus.PROPOSED) }
    }
}
