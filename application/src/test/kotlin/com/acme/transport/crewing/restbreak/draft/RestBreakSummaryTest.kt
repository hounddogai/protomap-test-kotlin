package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakSummaryTest {
    @Test
    fun `starts in requested`() {
        val item = newRestBreakSummary("code-705", "name-506")
        assertEquals(RestBreakSummaryStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newRestBreakSummary("code-705", "name-506")
        try {
            item.moveTo(RestBreakSummaryStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakSummaryStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRestBreakSummary("code-705", "name-506")
        item.status = RestBreakSummaryStatus.EXPIRED
        assertFails("move") { item.moveTo(RestBreakSummaryStatus.REQUESTED) }
    }
}
