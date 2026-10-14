package com.acme.transport.lastmile.redelivery.redeliveryquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class SeasonalRedeliveryGroupTest {
    @Test
    fun `starts in planned`() {
        val item = newSeasonalRedeliveryGroup("code-451", "label-605")
        assertEquals(SeasonalRedeliveryGroupStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to completed`() {
        val item = newSeasonalRedeliveryGroup("code-451", "label-605")
        try {
            item.moveTo(SeasonalRedeliveryGroupStatus.COMPLETED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(SeasonalRedeliveryGroupStatus.COMPLETED, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newSeasonalRedeliveryGroup("code-451", "label-605")
        item.status = SeasonalRedeliveryGroupStatus.CANCELLED
        assertFails("move") { item.moveTo(SeasonalRedeliveryGroupStatus.PLANNED) }
    }
}
