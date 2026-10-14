package com.acme.transport.lastmile.parcel.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ParcelSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newParcelSummary("code-150", "title-219")
        assertEquals(ParcelSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newParcelSummary("code-150", "title-219")
        try {
            item.moveTo(ParcelSummaryStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ParcelSummaryStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newParcelSummary("code-150", "title-219")
        item.status = ParcelSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(ParcelSummaryStatus.REQUESTED) }
    }
}
