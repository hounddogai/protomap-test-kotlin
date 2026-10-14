package com.acme.transport.terminal.traileryard.yardentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingTrailerYardPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newRollingTrailerYardPolicy("code-884", "title-318")
        assertEquals(RollingTrailerYardPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRollingTrailerYardPolicy("code-884", "title-318")
        try {
            item.moveTo(RollingTrailerYardPolicyStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingTrailerYardPolicyStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRollingTrailerYardPolicy("code-884", "title-318")
        item.status = RollingTrailerYardPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(RollingTrailerYardPolicyStatus.CREATED) }
    }
}
