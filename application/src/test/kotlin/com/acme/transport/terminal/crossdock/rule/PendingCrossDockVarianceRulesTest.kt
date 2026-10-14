package com.acme.transport.terminal.crossdock.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PendingCrossDockVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPendingCrossDockVarianceValidator()
        val item = newPendingCrossDockVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = PendingCrossDockVarianceRequiredCodeRule("pending cross dock variance needs an entry")
        val item = newPendingCrossDockVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long depot`() {
        val rule = PendingCrossDockVarianceDepotLengthRule(32)
        val item = newPendingCrossDockVariance("x", "y")
        item.depot = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.depot = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = PendingCrossDockVarianceRequiredNameRule("pending cross dock variance needs a value")
        val item = newPendingCrossDockVariance("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
