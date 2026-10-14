package com.acme.transport.lastmile.redelivery.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryScoreTest {
    @Test
    fun `starts in created`() {
        val item = newRedeliveryScore("code-922", "title-184")
        assertEquals(RedeliveryScoreStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newRedeliveryScore("code-922", "title-184")
        try {
            item.moveTo(RedeliveryScoreStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryScoreStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newRedeliveryScore("code-922", "title-184")
        item.status = RedeliveryScoreStatus.RETIRED
        assertFails("move") { item.moveTo(RedeliveryScoreStatus.CREATED) }
    }
}
