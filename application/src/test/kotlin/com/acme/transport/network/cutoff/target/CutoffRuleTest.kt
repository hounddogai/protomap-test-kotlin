package com.acme.transport.network.cutoff.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class CutoffRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newCutoffRule("code-744", "title-244")
        assertEquals(CutoffRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newCutoffRule("code-744", "title-244")
        try {
            item.moveTo(CutoffRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(CutoffRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newCutoffRule("code-744", "title-244")
        item.status = CutoffRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(CutoffRuleStatus.PROPOSED) }
    }
}
