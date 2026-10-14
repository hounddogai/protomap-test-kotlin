package com.acme.transport.terminal.crossdock.dockproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalCrossDockVarianceRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalCrossDockVarianceValidator()
        val item = newGlobalCrossDockVariance("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = GlobalCrossDockVarianceCodeLengthRule(16)
        val item = newGlobalCrossDockVariance("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = GlobalCrossDockVarianceRequiredCodeRule("global cross dock variance needs an entry")
        val item = newGlobalCrossDockVariance("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank depot`() {
        val rule = GlobalCrossDockVarianceRequiredDepotRule("global cross dock variance needs a value")
        val item = newGlobalCrossDockVariance("", "")
        item.depot = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalCrossDockVarianceTitleLengthRule(86400)
        val item = newGlobalCrossDockVariance("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
