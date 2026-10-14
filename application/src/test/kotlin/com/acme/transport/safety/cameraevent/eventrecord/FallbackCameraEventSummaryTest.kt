package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackCameraEventSummaryTest {
    @Test
    fun `starts in open`() {
        val item = newFallbackCameraEventSummary("code-185", "label-74")
        assertEquals(FallbackCameraEventSummaryStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to archived`() {
        val item = newFallbackCameraEventSummary("code-185", "label-74")
        try {
            item.moveTo(FallbackCameraEventSummaryStatus.ARCHIVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackCameraEventSummaryStatus.ARCHIVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newFallbackCameraEventSummary("code-185", "label-74")
        item.status = FallbackCameraEventSummaryStatus.ARCHIVED
        assertFails("move") { item.moveTo(FallbackCameraEventSummaryStatus.OPEN) }
    }
}
