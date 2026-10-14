package com.acme.transport.linehaul.trailer.regionaltrailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerTargetTest {
    @Test
    fun `starts in requested`() {
        val item = newManualTrailerTarget("code-621", "name-522")
        assertEquals(ManualTrailerTargetStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newManualTrailerTarget("code-621", "name-522")
        try {
            item.moveTo(ManualTrailerTargetStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerTargetStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newManualTrailerTarget("code-621", "name-522")
        item.status = ManualTrailerTargetStatus.EXPIRED
        assertFails("move") { item.moveTo(ManualTrailerTargetStatus.REQUESTED) }
    }
}
