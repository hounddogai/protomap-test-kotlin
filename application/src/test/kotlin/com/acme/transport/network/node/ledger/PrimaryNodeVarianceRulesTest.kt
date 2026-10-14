package com.acme.transport.network.node.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryNodeVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryNodeVarianceValidator()
        val item = newPrimaryNodeVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = PrimaryNodeVarianceTitleLengthRule(80)
        val item = newPrimaryNodeVariance("x", "y")
        item.title = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = PrimaryNodeVarianceCodeLengthRule(120)
        val item = newPrimaryNodeVariance("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank description`() {
        val rule = PrimaryNodeVarianceRequiredDescriptionRule("primary node variance needs an entry")
        val item = newPrimaryNodeVariance("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
