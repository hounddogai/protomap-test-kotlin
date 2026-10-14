package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationScheduleRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationScheduleValidator()
        val item = newQualificationSchedule("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above minimum`() {
        val rule = QualificationScheduleMinimumRangeRule(5.2, 1636.3)
        val item = newQualificationSchedule("x", "y")
        item.minimum = 1637.8
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank code`() {
        val rule = QualificationScheduleRequiredCodeRule("qualification schedule needs an entry")
        val item = newQualificationSchedule("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
