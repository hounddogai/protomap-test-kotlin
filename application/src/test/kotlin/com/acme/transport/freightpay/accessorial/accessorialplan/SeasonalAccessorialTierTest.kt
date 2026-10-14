package com.acme.transport.freightpay.accessorial.accessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalAccessorialTierTest {
    @Test
    fun `starts in new`() {
        val item = newSeasonalAccessorialTier("code-690", "label-136")
        assertEquals(SeasonalAccessorialTierStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newSeasonalAccessorialTier("code-690", "label-136")
        try {
            item.moveTo(SeasonalAccessorialTierStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalAccessorialTierStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newSeasonalAccessorialTier("code-690", "label-136")
        item.status = SeasonalAccessorialTierStatus.FAILED
        assertFails("move") { item.moveTo(SeasonalAccessorialTierStatus.NEW) }
    }
}
