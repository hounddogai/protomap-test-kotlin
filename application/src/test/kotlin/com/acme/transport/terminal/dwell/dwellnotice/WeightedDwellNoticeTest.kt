package com.acme.transport.terminal.dwell.dwellnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class WeightedDwellNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newWeightedDwellNotice("code-679", "title-648")
        assertEquals(WeightedDwellNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newWeightedDwellNotice("code-679", "title-648")
        try {
            item.moveTo(WeightedDwellNoticeStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(WeightedDwellNoticeStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newWeightedDwellNotice("code-679", "title-648")
        item.status = WeightedDwellNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(WeightedDwellNoticeStatus.REQUESTED) }
    }
}
