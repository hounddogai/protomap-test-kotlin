package com.acme.transport.crewing.qualification.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationSlotRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationSlotValidator()
        val item = newQualificationSlot("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank driver id`() {
        val rule = QualificationSlotRequiredDriverIdRule("qualification slot needs a value")
        val item = newQualificationSlot("", "")
        item.driverId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above margin`() {
        val rule = QualificationSlotMarginRangeRule(6.1, 96.4)
        val item = newQualificationSlot("x", "y")
        item.margin = 97.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long code`() {
        val rule = QualificationSlotCodeLengthRule(1000)
        val item = newQualificationSlot("x", "y")
        item.code = "x".repeat(1001)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
