package com.acme.transport.safety.scorecard.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalScorecardManifestTest {
    @Test
    fun `starts in planned`() {
        val item = newGlobalScorecardManifest("code-667", "name-348")
        assertEquals(GlobalScorecardManifestStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newGlobalScorecardManifest("code-667", "name-348")
        try {
            item.moveTo(GlobalScorecardManifestStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalScorecardManifestStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newGlobalScorecardManifest("code-667", "name-348")
        item.status = GlobalScorecardManifestStatus.CANCELLED
        assertFails("move") { item.moveTo(GlobalScorecardManifestStatus.PLANNED) }
    }
}
