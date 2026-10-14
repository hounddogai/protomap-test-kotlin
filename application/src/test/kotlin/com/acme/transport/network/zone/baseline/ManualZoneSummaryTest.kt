package com.acme.transport.network.zone.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualZoneSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualZoneSummary("code-256", "label-441")
        assertEquals(ManualZoneSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualZoneSummary("code-256", "label-441")
        try {
            item.moveTo(ManualZoneSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualZoneSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualZoneSummary("code-256", "label-441")
        item.status = ManualZoneSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualZoneSummaryStatus.PROPOSED) }
    }
}
