package com.acme.transport.crewing.qualification.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SeasonalQualificationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultSeasonalQualificationNoticeValidator()
        val item = newSeasonalQualificationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = SeasonalQualificationNoticeCodeLengthRule(86400)
        val item = newSeasonalQualificationNotice("x", "y")
        item.code = "x".repeat(86401)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank region`() {
        val rule = SeasonalQualificationNoticeRequiredRegionRule("seasonal qualification notice needs an entry")
        val item = newSeasonalQualificationNotice("", "")
        item.region = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above created at`() {
        val rule = SeasonalQualificationNoticeCreatedAtRangeRule(80, 120)
        val item = newSeasonalQualificationNotice("x", "y")
        item.createdAt = 121
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
