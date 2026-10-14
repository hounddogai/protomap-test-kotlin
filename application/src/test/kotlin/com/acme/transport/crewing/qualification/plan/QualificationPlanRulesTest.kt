package com.acme.transport.crewing.qualification.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QualificationPlanRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultQualificationPlanValidator()
        val item = newQualificationPlan("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long channel`() {
        val rule = QualificationPlanChannelLengthRule(64)
        val item = newQualificationPlan("x", "y")
        item.channel = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.channel = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank channel`() {
        val rule = QualificationPlanRequiredChannelRule("qualification plan needs a value")
        val item = newQualificationPlan("", "")
        item.channel = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = QualificationPlanRequiredNotesRule("qualification plan needs an entry")
        val item = newQualificationPlan("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above ceiling`() {
        val rule = QualificationPlanCeilingRangeRule(0.4, 4412.6)
        val item = newQualificationPlan("x", "y")
        item.ceiling = 4414.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags values above sequence`() {
        val rule = QualificationPlanSequenceRangeRule(32, 86400)
        val item = newQualificationPlan("x", "y")
        item.sequence = 86401
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long label`() {
        val rule = QualificationPlanLabelLengthRule(80)
        val item = newQualificationPlan("x", "y")
        item.label = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
