package com.acme.transport.safety.scorecard.scorecardbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryScorecardBundleTest {
    @Test
    fun `starts in requested`() {
        val item = newPrimaryScorecardBundle("code-917", "title-921")
        assertEquals(PrimaryScorecardBundleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPrimaryScorecardBundle("code-917", "title-921")
        try {
            item.moveTo(PrimaryScorecardBundleStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryScorecardBundleStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPrimaryScorecardBundle("code-917", "title-921")
        item.status = PrimaryScorecardBundleStatus.EXPIRED
        assertFails("move") { item.moveTo(PrimaryScorecardBundleStatus.REQUESTED) }
    }
}
