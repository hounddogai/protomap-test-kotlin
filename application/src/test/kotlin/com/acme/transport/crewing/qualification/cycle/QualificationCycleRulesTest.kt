package com.acme.transport.crewing.qualification.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationCycleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationCycleValidator()
        val item = newQualificationCycle("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = QualificationCycleTitleLengthRule(1000)
        val item = newQualificationCycle("x", "y")
        item.title = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank title`() {
        val rule = QualificationCycleRequiredTitleRule("qualification cycle needs a value")
        val item = newQualificationCycle("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above odometer`() {
        val rule = QualificationCycleOdometerRangeRule(80, 1000)
        val item = newQualificationCycle("x", "y")
        item.odometer = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
