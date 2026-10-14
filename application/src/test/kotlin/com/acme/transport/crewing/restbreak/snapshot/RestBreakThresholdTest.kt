package com.acme.transport.crewing.restbreak.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakThresholdTest {
    @Test
    fun `starts in open`() {
        val item = newRestBreakThreshold("code-653", "name-748")
        assertEquals(RestBreakThresholdStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newRestBreakThreshold("code-653", "name-748")
        try {
            item.moveTo(RestBreakThresholdStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakThresholdStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newRestBreakThreshold("code-653", "name-748")
        item.status = RestBreakThresholdStatus.ARCHIVED
        assertFails("move") { item.moveTo(RestBreakThresholdStatus.OPEN) }
    }
}
