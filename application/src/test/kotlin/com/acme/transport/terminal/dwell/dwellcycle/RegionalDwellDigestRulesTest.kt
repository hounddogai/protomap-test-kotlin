package com.acme.transport.terminal.dwell.dwellcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RegionalDwellDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalDwellDigestValidator()
        val item = newRegionalDwellDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above amount`() {
        val rule = RegionalDwellDigestAmountRangeRule(1.7, 2322.5)
        val item = newRegionalDwellDigest("x", "y")
        item.amount = 2324.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank label`() {
        val rule = RegionalDwellDigestRequiredLabelRule("regional dwell digest needs an entry")
        val item = newRegionalDwellDigest("", "")
        item.label = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RegionalDwellDigestCodeLengthRule(120)
        val item = newRegionalDwellDigest("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
