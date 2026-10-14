package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class NearMissSummaryTest {
    @Test
    fun `starts in proposed`() {
        val item = newNearMissSummary("code-343", "name-300")
        assertEquals(NearMissSummaryStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newNearMissSummary("code-343", "name-300")
        try {
            item.moveTo(NearMissSummaryStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(NearMissSummaryStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newNearMissSummary("code-343", "name-300")
        item.status = NearMissSummaryStatus.WITHDRAWN
        assertFails("move") { item.moveTo(NearMissSummaryStatus.PROPOSED) }
    }
}
