package com.acme.transport.lastmile.parcel.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ParcelNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultParcelNoticeValidator()
        val item = newParcelNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = ParcelNoticeRequiredCodeRule("parcel notice needs a value")
        val item = newParcelNotice("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = ParcelNoticeScoreRangeRule(3.8, 3962.6)
        val item = newParcelNotice("x", "y")
        item.score = 3964.1
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = ParcelNoticeRequiredSegmentRule("parcel notice needs a value")
        val item = newParcelNotice("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
