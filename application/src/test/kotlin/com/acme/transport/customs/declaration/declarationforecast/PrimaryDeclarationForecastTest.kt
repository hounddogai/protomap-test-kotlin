package com.acme.transport.customs.declaration.declarationforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryDeclarationForecastTest {
    @Test
    fun `starts in draft`() {
        val item = newPrimaryDeclarationForecast("code-353", "name-16")
        assertEquals(PrimaryDeclarationForecastStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newPrimaryDeclarationForecast("code-353", "name-16")
        try {
            item.moveTo(PrimaryDeclarationForecastStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryDeclarationForecastStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newPrimaryDeclarationForecast("code-353", "name-16")
        item.status = PrimaryDeclarationForecastStatus.CLOSED
        assertFails("move") { item.moveTo(PrimaryDeclarationForecastStatus.DRAFT) }
    }
}
