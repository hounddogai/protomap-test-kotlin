package com.acme.transport.linehaul.laneplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LanePlanManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newLanePlanManifest("code-283", "title-828")
        assertEquals(LanePlanManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newLanePlanManifest("code-283", "title-828")
        try {
            item.moveTo(LanePlanManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LanePlanManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newLanePlanManifest("code-283", "title-828")
        item.status = LanePlanManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(LanePlanManifestStatus.PROPOSED) }
    }
}
