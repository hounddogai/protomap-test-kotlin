package com.acme.transport.lastmile.attempt.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class AttemptRuleTest {
    @Test
    fun `starts in new`() {
        val item = newAttemptRule("code-513", "label-407")
        assertEquals(AttemptRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to done`() {
        val item = newAttemptRule("code-513", "label-407")
        try {
            item.moveTo(AttemptRuleStatus.DONE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(AttemptRuleStatus.DONE, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newAttemptRule("code-513", "label-407")
        item.status = AttemptRuleStatus.FAILED
        assertFails("move") { item.moveTo(AttemptRuleStatus.NEW) }
    }
}
