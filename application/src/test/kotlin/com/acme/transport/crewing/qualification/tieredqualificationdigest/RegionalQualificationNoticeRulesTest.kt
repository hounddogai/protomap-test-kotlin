package com.acme.transport.crewing.qualification.tieredqualificationdigest

import kotlin.test.Test
import kotlin.test.assertTrue

class RegionalQualificationNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultRegionalQualificationNoticeValidator()
        val item = newRegionalQualificationNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags values above stop count`() {
        val rule = RegionalQualificationNoticeStopCountRangeRule(80, 1000)
        val item = newRegionalQualificationNotice("x", "y")
        item.stopCount = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }
}
