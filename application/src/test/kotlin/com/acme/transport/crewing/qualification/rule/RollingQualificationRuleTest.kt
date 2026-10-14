package com.acme.transport.crewing.qualification.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingQualificationRuleTest {
    @Test
    fun `starts in new`() {
        val item = newRollingQualificationRule("code-528", "title-503")
        assertEquals(RollingQualificationRuleStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to running`() {
        val item = newRollingQualificationRule("code-528", "title-503")
        try {
            item.moveTo(RollingQualificationRuleStatus.RUNNING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingQualificationRuleStatus.RUNNING, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newRollingQualificationRule("code-528", "title-503")
        item.status = RollingQualificationRuleStatus.FAILED
        assertFails("move") { item.moveTo(RollingQualificationRuleStatus.NEW) }
    }
}
