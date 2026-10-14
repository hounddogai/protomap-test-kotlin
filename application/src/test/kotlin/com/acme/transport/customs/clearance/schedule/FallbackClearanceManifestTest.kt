package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class FallbackClearanceManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newFallbackClearanceManifest("code-289", "label-187")
        assertEquals(FallbackClearanceManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newFallbackClearanceManifest("code-289", "label-187")
        try {
            item.moveTo(FallbackClearanceManifestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(FallbackClearanceManifestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newFallbackClearanceManifest("code-289", "label-187")
        item.status = FallbackClearanceManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(FallbackClearanceManifestStatus.PLANNED) }
    }
}
