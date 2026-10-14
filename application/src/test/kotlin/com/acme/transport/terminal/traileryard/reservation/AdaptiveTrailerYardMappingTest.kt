package com.acme.transport.terminal.traileryard.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AdaptiveTrailerYardMappingTest {
    @Test
    fun `starts in new`() {
        val item = newAdaptiveTrailerYardMapping("code-478", "title-264")
        assertEquals(AdaptiveTrailerYardMappingStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newAdaptiveTrailerYardMapping("code-478", "title-264")
        try {
            item.moveTo(AdaptiveTrailerYardMappingStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AdaptiveTrailerYardMappingStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAdaptiveTrailerYardMapping("code-478", "title-264")
        item.status = AdaptiveTrailerYardMappingStatus.FAILED
        assertFails("move") { item.moveTo(AdaptiveTrailerYardMappingStatus.NEW) }
    }
}
