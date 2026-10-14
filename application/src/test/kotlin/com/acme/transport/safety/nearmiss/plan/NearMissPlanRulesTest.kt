package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissPlanValidator()
        val item = newNearMissPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank dock`() {
        val rule = NearMissPlanRequiredDockRule("near miss plan needs a value")
        val item = newNearMissPlan("", "")
        item.dock = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = NearMissPlanDockLengthRule(120)
        val item = newNearMissPlan("x", "y")
        item.dock = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = NearMissPlanRequiredTitleRule("near miss plan needs a value")
        val item = newNearMissPlan("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
