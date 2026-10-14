package com.acme.transport.linehaul.trailer.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TrailerThresholdTest {
    @Test
    fun `starts in created`() {
        val item = newTrailerThreshold("code-746", "name-23")
        assertEquals(TrailerThresholdStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newTrailerThreshold("code-746", "name-23")
        try {
            item.moveTo(TrailerThresholdStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TrailerThresholdStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newTrailerThreshold("code-746", "name-23")
        item.status = TrailerThresholdStatus.RETIRED
        assertFails("move") { item.moveTo(TrailerThresholdStatus.CREATED) }
    }
}
