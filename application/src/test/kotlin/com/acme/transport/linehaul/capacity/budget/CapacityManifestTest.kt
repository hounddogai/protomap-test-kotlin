package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CapacityManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newCapacityManifest("code-547", "title-514")
        assertEquals(CapacityManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newCapacityManifest("code-547", "title-514")
        try {
            item.moveTo(CapacityManifestStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CapacityManifestStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newCapacityManifest("code-547", "title-514")
        item.status = CapacityManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(CapacityManifestStatus.PLANNED) }
    }
}
