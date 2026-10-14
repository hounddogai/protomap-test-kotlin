package com.acme.transport.fleetops.tire.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TireRuleTest {
    @Test
    fun `starts in proposed`() {
        val item = newTireRule("code-260", "title-282")
        assertEquals(TireRuleStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newTireRule("code-260", "title-282")
        try {
            item.moveTo(TireRuleStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TireRuleStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newTireRule("code-260", "title-282")
        item.status = TireRuleStatus.WITHDRAWN
        assertFails("move") { item.moveTo(TireRuleStatus.PROPOSED) }
    }
}
