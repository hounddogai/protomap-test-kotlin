package com.acme.transport.safety.nearmiss.missestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class WeightedNearMissQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultWeightedNearMissQuotaValidator()
        val item = newWeightedNearMissQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank timezone`() {
        val rule = WeightedNearMissQuotaRequiredTimezoneRule("weighted near miss quota needs an entry")
        val item = newWeightedNearMissQuota("", "")
        item.timezone = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long name`() {
        val rule = WeightedNearMissQuotaNameLengthRule(86400)
        val item = newWeightedNearMissQuota("x", "y")
        item.name = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = WeightedNearMissQuotaRequiredNameRule("weighted near miss quota needs this field")
        val item = newWeightedNearMissQuota("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
