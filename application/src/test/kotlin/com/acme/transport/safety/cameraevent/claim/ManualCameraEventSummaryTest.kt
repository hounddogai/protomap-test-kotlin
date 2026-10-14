package com.acme.transport.safety.cameraevent.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventSummaryTest {
    @Test
    fun `starts in new`() {
        val item = newManualCameraEventSummary("code-169", "name-164")
        assertEquals(ManualCameraEventSummaryStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newManualCameraEventSummary("code-169", "name-164")
        try {
            item.moveTo(ManualCameraEventSummaryStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventSummaryStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualCameraEventSummary("code-169", "name-164")
        item.status = ManualCameraEventSummaryStatus.FAILED
        assertFails("move") { item.moveTo(ManualCameraEventSummaryStatus.NEW) }
    }
}
