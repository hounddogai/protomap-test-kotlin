package com.acme.transport.linehaul.trailer.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerMappingTest {
    @Test
    fun `starts in idle`() {
        val item = newTrailerMapping("code-298", "title-254")
        assertEquals(TrailerMappingStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newTrailerMapping("code-298", "title-254")
        try {
            item.moveTo(TrailerMappingStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerMappingStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newTrailerMapping("code-298", "title-254")
        item.status = TrailerMappingStatus.STOPPED
        assertFails("move") { item.moveTo(TrailerMappingStatus.IDLE) }
    }
}
