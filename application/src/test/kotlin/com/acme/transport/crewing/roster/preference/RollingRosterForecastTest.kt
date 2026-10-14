package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingRosterForecastTest {
    @Test
    fun `starts in open`() {
        val item = newRollingRosterForecast("code-468", "title-996")
        assertEquals(RollingRosterForecastStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newRollingRosterForecast("code-468", "title-996")
        try {
            item.moveTo(RollingRosterForecastStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingRosterForecastStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRollingRosterForecast("code-468", "title-996")
        item.status = RollingRosterForecastStatus.ARCHIVED
        assertFails("move") { item.moveTo(RollingRosterForecastStatus.OPEN) }
    }
}
