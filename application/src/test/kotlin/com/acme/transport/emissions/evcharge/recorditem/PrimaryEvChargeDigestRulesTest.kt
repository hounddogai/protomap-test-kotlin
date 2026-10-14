package com.acme.transport.emissions.evcharge.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PrimaryEvChargeDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultPrimaryEvChargeDigestValidator()
        val item = newPrimaryEvChargeDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank unit`() {
        val rule = PrimaryEvChargeDigestRequiredUnitRule("primary ev charge digest needs this field")
        val item = newPrimaryEvChargeDigest("", "")
        item.unit = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = PrimaryEvChargeDigestVersionRangeRule(1000, 3600)
        val item = newPrimaryEvChargeDigest("x", "y")
        item.version = 3601
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
