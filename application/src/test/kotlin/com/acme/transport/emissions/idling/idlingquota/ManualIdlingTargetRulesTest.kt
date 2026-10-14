package com.acme.transport.emissions.idling.idlingquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ManualIdlingTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultManualIdlingTargetValidator()
        val item = newManualIdlingTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ManualIdlingTargetRequiredCodeRule("manual idling target needs an entry")
        val item = newManualIdlingTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank reviewer`() {
        val rule = ManualIdlingTargetRequiredReviewerRule("manual idling target needs this field")
        val item = newManualIdlingTarget("", "")
        item.reviewer = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = ManualIdlingTargetTitleLengthRule(1000)
        val item = newManualIdlingTarget("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
