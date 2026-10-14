package com.acme.transport.freightpay.dispute.disputepolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalDisputeTierTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalDisputeTier("code-422", "title-938")
        assertEquals(SeasonalDisputeTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalDisputeTier("code-422", "title-938")
        try {
            item.moveTo(SeasonalDisputeTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalDisputeTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalDisputeTier("code-422", "title-938")
        item.status = SeasonalDisputeTierStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalDisputeTierStatus.NEW) }
    }
}
