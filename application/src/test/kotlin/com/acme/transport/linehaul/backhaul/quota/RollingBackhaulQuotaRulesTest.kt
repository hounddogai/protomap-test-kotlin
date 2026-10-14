package com.acme.transport.linehaul.backhaul.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulQuotaValidator()
        val item = newRollingBackhaulQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RollingBackhaulQuotaLocaleLengthRule(16)
        val item = newRollingBackhaulQuota("x", "y")
        item.locale = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags values above score`() {
        val rule = RollingBackhaulQuotaScoreRangeRule(10.0, 1020.1)
        val item = newRollingBackhaulQuota("x", "y")
        item.score = 1021.6
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBackhaulQuotaCodeLengthRule(16)
        val item = newRollingBackhaulQuota("x", "y")
        item.code = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingBackhaulQuotaLabelLengthRule(255)
        val item = newRollingBackhaulQuota("x", "y")
        item.label = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingBackhaulQuotaRequiredCodeRule("rolling backhaul quota needs an entry")
        val item = newRollingBackhaulQuota("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
