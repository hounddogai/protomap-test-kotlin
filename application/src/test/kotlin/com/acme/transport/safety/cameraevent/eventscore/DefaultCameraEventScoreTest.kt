package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultCameraEventScoreTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultCameraEventScore("code-441", "label-60")
        assertEquals(DefaultCameraEventScoreStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultCameraEventScore("code-441", "label-60")
        try {
            item.moveTo(DefaultCameraEventScoreStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultCameraEventScoreStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultCameraEventScore("code-441", "label-60")
        item.status = DefaultCameraEventScoreStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultCameraEventScoreStatus.PROPOSED) }
    }
}
