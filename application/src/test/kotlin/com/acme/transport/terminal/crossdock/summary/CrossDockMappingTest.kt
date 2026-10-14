package com.acme.transport.terminal.crossdock.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CrossDockMappingTest {
    @Test
    fun `starts in planned`() {
        val item = newCrossDockMapping("code-682", "title-690")
        assertEquals(CrossDockMappingStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to scheduled`() {
        val item = newCrossDockMapping("code-682", "title-690")
        try {
            item.moveTo(CrossDockMappingStatus.SCHEDULED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CrossDockMappingStatus.SCHEDULED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCrossDockMapping("code-682", "title-690")
        item.status = CrossDockMappingStatus.CANCELLED
        assertFails("move") { item.moveTo(CrossDockMappingStatus.PLANNED) }
    }
}
