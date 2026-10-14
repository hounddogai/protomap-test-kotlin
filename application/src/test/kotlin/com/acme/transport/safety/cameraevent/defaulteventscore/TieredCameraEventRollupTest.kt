package com.acme.transport.safety.cameraevent.defaulteventscore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredCameraEventRollupTest {
    @Test
    fun `starts in open`() {
        val item = newTieredCameraEventRollup("code-836", "label-430")
        assertEquals(TieredCameraEventRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newTieredCameraEventRollup("code-836", "label-430")
        try {
            item.moveTo(TieredCameraEventRollupStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredCameraEventRollupStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredCameraEventRollup("code-836", "label-430")
        item.status = TieredCameraEventRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredCameraEventRollupStatus.OPEN) }
    }
}
