package com.acme.transport.linehaul.backhaul.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BackhaulForecastTest {
    @Test
    fun `starts in proposed`() {
        val item = newBackhaulForecast("code-398", "title-40")
        assertEquals(BackhaulForecastStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newBackhaulForecast("code-398", "title-40")
        try {
            item.moveTo(BackhaulForecastStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BackhaulForecastStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBackhaulForecast("code-398", "title-40")
        item.status = BackhaulForecastStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BackhaulForecastStatus.PROPOSED) }
    }
}
