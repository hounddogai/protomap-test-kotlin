package com.acme.transport.customs.declaration.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingDeclarationForecastTest {
    @Test
    fun `starts in planned`() {
        val item = newPendingDeclarationForecast("code-269", "title-655")
        assertEquals(PendingDeclarationForecastStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newPendingDeclarationForecast("code-269", "title-655")
        try {
            item.moveTo(PendingDeclarationForecastStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingDeclarationForecastStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newPendingDeclarationForecast("code-269", "title-655")
        item.status = PendingDeclarationForecastStatus.CANCELLED
        assertFails("move") { item.moveTo(PendingDeclarationForecastStatus.PLANNED) }
    }
}
