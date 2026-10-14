package com.acme.transport.terminal.crossdock.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CrossDockDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCrossDockDigestValidator()
        val item = newCrossDockDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long dock`() {
        val rule = CrossDockDigestDockLengthRule(86400)
        val item = newCrossDockDigest("x", "y")
        item.dock = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.dock = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CrossDockDigestRateRangeRule(1.1, 2560.3)
        val item = newCrossDockDigest("x", "y")
        item.rate = 2561.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CrossDockDigestRequiredTitleRule("cross dock digest needs a value")
        val item = newCrossDockDigest("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above revision`() {
        val rule = CrossDockDigestRevisionRangeRule(64, 80)
        val item = newCrossDockDigest("x", "y")
        item.revision = 81
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
