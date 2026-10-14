package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalDropTierTest {
    @Test
    fun `starts in open`() {
        val item = newRegionalDropTier("code-115", "title-931")
        assertEquals(RegionalDropTierStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newRegionalDropTier("code-115", "title-931")
        try {
            item.moveTo(RegionalDropTierStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalDropTierStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRegionalDropTier("code-115", "title-931")
        item.status = RegionalDropTierStatus.ARCHIVED
        assertFails("move") { item.moveTo(RegionalDropTierStatus.OPEN) }
    }
}
