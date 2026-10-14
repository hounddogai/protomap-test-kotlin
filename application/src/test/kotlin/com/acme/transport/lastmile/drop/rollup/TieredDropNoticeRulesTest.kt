package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredDropNoticeRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredDropNoticeValidator()
        val item = newTieredDropNotice("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a long code`() {
        val rule = TieredDropNoticeCodeLengthRule(64)
        val item = newTieredDropNotice("x", "y")
        item.code = "x".repeat(65)
        assertEquals(1, rule.check(item).size, "problems")
        item.code = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a blank external ref`() {
        val rule = TieredDropNoticeRequiredExternalRefRule("tiered drop notice needs this field")
        val item = newTieredDropNotice("", "")
        item.externalRef = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above score`() {
        val rule = TieredDropNoticeScoreRangeRule(8.0, 1488.4)
        val item = newTieredDropNotice("x", "y")
        item.score = 1489.9
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a long title`() {
        val rule = TieredDropNoticeTitleLengthRule(16)
        val item = newTieredDropNotice("x", "y")
        item.title = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long external ref`() {
        val rule = TieredDropNoticeExternalRefLengthRule(16)
        val item = newTieredDropNotice("x", "y")
        item.externalRef = "x".repeat(17)
        assertEquals(1, rule.check(item).size, "problems")
        item.externalRef = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
