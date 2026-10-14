package com.acme.transport.network.servicearea.areaassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaSnapshotTest {
    @Test
    fun `starts in proposed`() {
        val item = newServiceAreaSnapshot("code-440", "title-54")
        assertEquals(ServiceAreaSnapshotStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newServiceAreaSnapshot("code-440", "title-54")
        try {
            item.moveTo(ServiceAreaSnapshotStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaSnapshotStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServiceAreaSnapshot("code-440", "title-54")
        item.status = ServiceAreaSnapshotStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServiceAreaSnapshotStatus.PROPOSED) }
    }
}
