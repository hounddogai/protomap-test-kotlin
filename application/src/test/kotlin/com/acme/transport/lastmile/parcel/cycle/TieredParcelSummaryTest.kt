package com.acme.transport.lastmile.parcel.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredParcelSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newTieredParcelSummary("code-529", "name-242")
        assertEquals(TieredParcelSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newTieredParcelSummary("code-529", "name-242")
        try {
            item.moveTo(TieredParcelSummaryStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredParcelSummaryStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newTieredParcelSummary("code-529", "name-242")
        item.status = TieredParcelSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(TieredParcelSummaryStatus.REQUESTED) }
    }
}
