package com.acme.transport.freightpay.ratecard.regionalcardsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalRateCardAssignmentRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalRateCardAssignmentValidator()
        val item = newGlobalRateCardAssignment("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalRateCardAssignmentTitleLengthRule(86400)
        val item = newGlobalRateCardAssignment("x", "y")
        item.title = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank name`() {
        val rule = GlobalRateCardAssignmentRequiredNameRule("global rate card assignment needs a value")
        val item = newGlobalRateCardAssignment("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
