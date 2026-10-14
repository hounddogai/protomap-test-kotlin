package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualCameraEventBundleTest {
    @Test
    fun `starts in new`() {
        val item = newManualCameraEventBundle("code-825", "name-503")
        assertEquals(ManualCameraEventBundleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newManualCameraEventBundle("code-825", "name-503")
        try {
            item.moveTo(ManualCameraEventBundleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualCameraEventBundleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualCameraEventBundle("code-825", "name-503")
        item.status = ManualCameraEventBundleStatus.FAILED
        assertFails("move") { item.moveTo(ManualCameraEventBundleStatus.NEW) }
    }
}
