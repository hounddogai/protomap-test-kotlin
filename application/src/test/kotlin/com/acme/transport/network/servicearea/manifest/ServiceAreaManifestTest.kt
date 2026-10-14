package com.acme.transport.network.servicearea.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ServiceAreaManifestTest {
    @Test
    fun `starts in idle`() {
        val item = newServiceAreaManifest("code-702", "title-144")
        assertEquals(ServiceAreaManifestStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newServiceAreaManifest("code-702", "title-144")
        try {
            item.moveTo(ServiceAreaManifestStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ServiceAreaManifestStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newServiceAreaManifest("code-702", "title-144")
        item.status = ServiceAreaManifestStatus.STOPPED
        assertFails("move") { item.moveTo(ServiceAreaManifestStatus.IDLE) }
    }
}
