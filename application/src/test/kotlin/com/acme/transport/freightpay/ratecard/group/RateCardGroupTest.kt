package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RateCardGroupTest {
    @Test
    fun `starts in created`() {
        val item = newRateCardGroup("code-365", "title-686")
        assertEquals(RateCardGroupStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newRateCardGroup("code-365", "title-686")
        try {
            item.moveTo(RateCardGroupStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RateCardGroupStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRateCardGroup("code-365", "title-686")
        item.status = RateCardGroupStatus.RETIRED
        assertFails("move") { item.moveTo(RateCardGroupStatus.CREATED) }
    }
}
