package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalServicePlanManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        assertEquals(GlobalServicePlanManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        try {
            item.moveTo(GlobalServicePlanManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalServicePlanManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        item.status = GlobalServicePlanManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(GlobalServicePlanManifestStatus.PROPOSED) }
    }
}
