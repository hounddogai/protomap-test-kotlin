package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RegionalCameraEventProfileTest {
    @Test
    fun `starts in new`() {
        val item = newRegionalCameraEventProfile("code-373", "title-128")
        assertEquals(RegionalCameraEventProfileStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRegionalCameraEventProfile("code-373", "title-128")
        try {
            item.moveTo(RegionalCameraEventProfileStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RegionalCameraEventProfileStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRegionalCameraEventProfile("code-373", "title-128")
        item.status = RegionalCameraEventProfileStatus.FAILED
        assertFails("move") { item.moveTo(RegionalCameraEventProfileStatus.NEW) }
    }
}
