package com.acme.transport.network.cutoff.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CappedCutoffMilestoneTest {
    @Test
    fun `starts in proposed`() {
        val item = newCappedCutoffMilestone("code-941", "name-488")
        assertEquals(CappedCutoffMilestoneStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to withdrawn`() {
        val item = newCappedCutoffMilestone("code-941", "name-488")
        try {
            item.moveTo(CappedCutoffMilestoneStatus.WITHDRAWN)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CappedCutoffMilestoneStatus.WITHDRAWN, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCappedCutoffMilestone("code-941", "name-488")
        item.status = CappedCutoffMilestoneStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CappedCutoffMilestoneStatus.PROPOSED) }
    }
}
