package com.acme.transport.safety.nearmiss.missrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PrimaryNearMissRuleTest {
    @Test
    fun `starts in created`() {
        val item = newPrimaryNearMissRule("code-931", "title-602")
        assertEquals(PrimaryNearMissRuleStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newPrimaryNearMissRule("code-931", "title-602")
        try {
            item.moveTo(PrimaryNearMissRuleStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PrimaryNearMissRuleStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newPrimaryNearMissRule("code-931", "title-602")
        item.status = PrimaryNearMissRuleStatus.RETIRED
        assertFails("move") { item.moveTo(PrimaryNearMissRuleStatus.CREATED) }
    }
}
