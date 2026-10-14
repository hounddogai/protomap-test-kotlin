package com.acme.transport.freightpay.ratecard.cardgroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyRateCardGroupTest {
    @Test
    fun `starts in created`() {
        val item = newLegacyRateCardGroup("code-396", "name-208")
        assertEquals(LegacyRateCardGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newLegacyRateCardGroup("code-396", "name-208")
        try {
            item.moveTo(LegacyRateCardGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyRateCardGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newLegacyRateCardGroup("code-396", "name-208")
        item.status = LegacyRateCardGroupStatus.RETIRED
        assertFails("move") { item.moveTo(LegacyRateCardGroupStatus.CREATED) }
    }
}
