package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaCheckpointTest {
    @Test
    fun `starts in proposed`() {
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        assertEquals(ServiceAreaCheckpointStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        try {
            item.moveTo(ServiceAreaCheckpointStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaCheckpointStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newServiceAreaCheckpoint("code-920", "label-834")
        item.status = ServiceAreaCheckpointStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ServiceAreaCheckpointStatus.PROPOSED) }
    }
}
