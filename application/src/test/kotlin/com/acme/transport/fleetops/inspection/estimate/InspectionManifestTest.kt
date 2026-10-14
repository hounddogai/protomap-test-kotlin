package com.acme.transport.fleetops.inspection.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class InspectionManifestTest {
    @Test
    fun `starts in created`() {
        val item = newInspectionManifest("code-230", "title-286")
        assertEquals(InspectionManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newInspectionManifest("code-230", "title-286")
        try {
            item.moveTo(InspectionManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(InspectionManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newInspectionManifest("code-230", "title-286")
        item.status = InspectionManifestStatus.RETIRED
        assertFails("move") { item.moveTo(InspectionManifestStatus.CREATED) }
    }
}
