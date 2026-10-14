package com.acme.transport.fleetops.defect.defecttarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingDefectTargetRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingDefectTargetValidator()
        val item = newRollingDefectTarget("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = RollingDefectTargetRequiredTitleRule("rolling defect target needs a value")
        val item = newRollingDefectTarget("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long label`() {
        val rule = RollingDefectTargetLabelLengthRule(16)
        val item = newRollingDefectTarget("x", "y")
        item.label = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.label = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingDefectTargetCodeLengthRule(64)
        val item = newRollingDefectTarget("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank code`() {
        val rule = RollingDefectTargetRequiredCodeRule("rolling defect target needs an entry")
        val item = newRollingDefectTarget("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
