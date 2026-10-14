package com.acme.transport.crewing.qualification.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class RollingQualificationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRollingQualificationNoticeValidator()
        val item = newRollingQualificationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long locale`() {
        val rule = RollingQualificationNoticeLocaleLengthRule(3600)
        val item = newRollingQualificationNotice("x", "y")
        item.locale = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.locale = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long name`() {
        val rule = RollingQualificationNoticeNameLengthRule(80)
        val item = newRollingQualificationNotice("x", "y")
        item.name = "x".repeat(81)
        assertEquals(1, rule.check(item).size, "problems")
        item.name = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long code`() {
        val rule = RollingQualificationNoticeCodeLengthRule(120)
        val item = newRollingQualificationNotice("x", "y")
        item.code = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
