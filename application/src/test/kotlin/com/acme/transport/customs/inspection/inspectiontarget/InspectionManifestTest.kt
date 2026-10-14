package com.acme.transport.customs.inspection.inspectiontarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionManifestTest {
    @Test
    fun `starts in new`() {
        val item = newInspectionManifest("code-391", "name-84")
        assertEquals(InspectionManifestStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newInspectionManifest("code-391", "name-84")
        try {
            item.moveTo(InspectionManifestStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionManifestStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newInspectionManifest("code-391", "name-84")
        item.status = InspectionManifestStatus.FAILED
        assertFails("move") { item.moveTo(InspectionManifestStatus.NEW) }
    }
}
