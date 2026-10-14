package com.acme.transport.safety.cameraevent.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CameraEventSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newCameraEventSummary("code-285", "title-798")
        assertEquals(CameraEventSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newCameraEventSummary("code-285", "title-798")
        try {
            item.moveTo(CameraEventSummaryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CameraEventSummaryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newCameraEventSummary("code-285", "title-798")
        item.status = CameraEventSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(CameraEventSummaryStatus.REQUESTED) }
    }
}
