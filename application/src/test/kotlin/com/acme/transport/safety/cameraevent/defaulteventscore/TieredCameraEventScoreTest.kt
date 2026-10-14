package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCameraEventScoreTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredCameraEventScore("code-268", "title-355")
        assertEquals(TieredCameraEventScoreStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newTieredCameraEventScore("code-268", "title-355")
        try {
            item.moveTo(TieredCameraEventScoreStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCameraEventScoreStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredCameraEventScore("code-268", "title-355")
        item.status = TieredCameraEventScoreStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredCameraEventScoreStatus.REQUESTED) }
    }
}
