package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventDigestTest {
    @Test
    fun `starts in new`() {
        val item = newManualCameraEventDigest("code-571", "label-464")
        assertEquals(ManualCameraEventDigestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newManualCameraEventDigest("code-571", "label-464")
        try {
            item.moveTo(ManualCameraEventDigestStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventDigestStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualCameraEventDigest("code-571", "label-464")
        item.status = ManualCameraEventDigestStatus.FAILED
        assertFails("move") { item.moveTo(ManualCameraEventDigestStatus.NEW) }
    }
}
