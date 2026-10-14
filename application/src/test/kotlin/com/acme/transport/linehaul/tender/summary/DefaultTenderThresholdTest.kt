package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultTenderThresholdTest {
    @Test
    fun `starts in planned`() {
        val item = newDefaultTenderThreshold("code-697", "label-17")
        assertEquals(DefaultTenderThresholdStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newDefaultTenderThreshold("code-697", "label-17")
        try {
            item.moveTo(DefaultTenderThresholdStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultTenderThresholdStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newDefaultTenderThreshold("code-697", "label-17")
        item.status = DefaultTenderThresholdStatus.CANCELLED
        assertFails("move") { item.moveTo(DefaultTenderThresholdStatus.PLANNED) }
    }
}
