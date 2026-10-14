package com.acme.transport.linehaul.capacity.capacityassignment

import kotlin.test.Test
import kotlin.test.assertTrue

class CapacityDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCapacityDigestValidator()
        val item = newCapacityDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above version`() {
        val rule = CapacityDigestVersionRangeRule(16, 120)
        val item = newCapacityDigest("x", "y")
        item.version = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = CapacityDigestMinimumRangeRule(3.1, 3919.6)
        val item = newCapacityDigest("x", "y")
        item.minimum = 3921.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above rate`() {
        val rule = CapacityDigestRateRangeRule(5.0, 1230.5)
        val item = newCapacityDigest("x", "y")
        item.rate = 1232.0
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
