package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDeclarationTierTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        assertEquals(SeasonalDeclarationTierStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        try {
            item.moveTo(SeasonalDeclarationTierStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDeclarationTierStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalDeclarationTier("code-241", "title-954")
        item.status = SeasonalDeclarationTierStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalDeclarationTierStatus.PLANNED) }
    }
}
