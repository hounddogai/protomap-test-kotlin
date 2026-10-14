package com.acme.transport.crewing.swap.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalSwapNoticeTest {
    @Test
    fun `starts in requested`() {
        val item = newRegionalSwapNotice("code-93", "label-864")
        assertEquals(RegionalSwapNoticeStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRegionalSwapNotice("code-93", "label-864")
        try {
            item.moveTo(RegionalSwapNoticeStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalSwapNoticeStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRegionalSwapNotice("code-93", "label-864")
        item.status = RegionalSwapNoticeStatus.EXPIRED
        assertFails("move") { item.moveTo(RegionalSwapNoticeStatus.REQUESTED) }
    }
}
