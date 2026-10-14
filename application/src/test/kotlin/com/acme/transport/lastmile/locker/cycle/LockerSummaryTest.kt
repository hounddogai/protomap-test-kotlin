package com.acme.transport.lastmile.locker.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LockerSummaryTest {
    @Test
    fun `starts in draft`() {
        val item = newLockerSummary("code-413", "name-693")
        assertEquals(LockerSummaryStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLockerSummary("code-413", "name-693")
        try {
            item.moveTo(LockerSummaryStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LockerSummaryStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLockerSummary("code-413", "name-693")
        item.status = LockerSummaryStatus.CLOSED
        assertFails("move") { item.moveTo(LockerSummaryStatus.DRAFT) }
    }
}
