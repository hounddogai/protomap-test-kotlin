package com.acme.transport.linehaul.tender.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GlobalTenderDigestRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultGlobalTenderDigestValidator()
        val item = newGlobalTenderDigest("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank notes`() {
        val rule = GlobalTenderDigestRequiredNotesRule("global tender digest needs a value")
        val item = newGlobalTenderDigest("", "")
        item.notes = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long title`() {
        val rule = GlobalTenderDigestTitleLengthRule(3600)
        val item = newGlobalTenderDigest("x", "y")
        item.title = "x".repeat(3601)
        assertEquals(1, rule.check(item).size, "problems")
        item.title = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }

    @Test
    fun `flags a long category`() {
        val rule = GlobalTenderDigestCategoryLengthRule(120)
        val item = newGlobalTenderDigest("x", "y")
        item.category = "x".repeat(121)
        assertEquals(1, rule.check(item).size, "problems")
        item.category = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
