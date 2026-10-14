package com.acme.transport.crewing.restbreak.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RestBreakClaimTest {
    @Test
    fun `starts in new`() {
        val item = newRestBreakClaim("code-373", "label-425")
        assertEquals(RestBreakClaimStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRestBreakClaim("code-373", "label-425")
        try {
            item.moveTo(RestBreakClaimStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RestBreakClaimStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRestBreakClaim("code-373", "label-425")
        item.status = RestBreakClaimStatus.FAILED
        assertFails("move") { item.moveTo(RestBreakClaimStatus.NEW) }
    }
}
