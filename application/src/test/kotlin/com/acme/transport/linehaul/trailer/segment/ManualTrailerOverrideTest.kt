package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualTrailerOverrideTest {
    @Test
    fun `starts in new`() {
        val item = newManualTrailerOverride("code-698", "title-992")
        assertEquals(ManualTrailerOverrideStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newManualTrailerOverride("code-698", "title-992")
        try {
            item.moveTo(ManualTrailerOverrideStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualTrailerOverrideStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newManualTrailerOverride("code-698", "title-992")
        item.status = ManualTrailerOverrideStatus.FAILED
        assertFails("move") { item.moveTo(ManualTrailerOverrideStatus.NEW) }
    }
}
