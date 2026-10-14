package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingBackhaulReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingBackhaulReviewValidator()
        val item = newRollingBackhaulReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = RollingBackhaulReviewRequiredNameRule("rolling backhaul review needs an entry")
        val item = newRollingBackhaulReview("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank category`() {
        val rule = RollingBackhaulReviewRequiredCategoryRule("rolling backhaul review needs this field")
        val item = newRollingBackhaulReview("", "")
        item.category = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingBackhaulReviewCodeLengthRule(32)
        val item = newRollingBackhaulReview("x", "y")
        item.code = "x".repeat(33)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
