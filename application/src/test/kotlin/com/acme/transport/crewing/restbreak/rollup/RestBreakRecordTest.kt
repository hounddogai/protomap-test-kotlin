package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakRecordTest {
    @Test
    fun `starts in proposed`() {
        val item = newRestBreakRecord("code-740", "label-52")
        assertEquals(RestBreakRecordStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to rejected`() {
        val item = newRestBreakRecord("code-740", "label-52")
        try {
            item.moveTo(RestBreakRecordStatus.REJECTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakRecordStatus.REJECTED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newRestBreakRecord("code-740", "label-52")
        item.status = RestBreakRecordStatus.WITHDRAWN
        assertFails("move") { item.moveTo(RestBreakRecordStatus.PROPOSED) }
    }
}
