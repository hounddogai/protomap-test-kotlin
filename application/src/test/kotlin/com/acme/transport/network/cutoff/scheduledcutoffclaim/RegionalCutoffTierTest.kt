package com.acme.transport.network.cutoff.scheduledcutoffclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCutoffTierTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalCutoffTier("code-921", "title-801")
        assertEquals(RegionalCutoffTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newRegionalCutoffTier("code-921", "title-801")
        try {
            item.moveTo(RegionalCutoffTierStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCutoffTierStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalCutoffTier("code-921", "title-801")
        item.status = RegionalCutoffTierStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalCutoffTierStatus.REQUESTED) }
    }
}
