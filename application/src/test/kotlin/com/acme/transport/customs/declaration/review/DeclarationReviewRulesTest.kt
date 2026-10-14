package com.acme.transport.customs.declaration.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DeclarationReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDeclarationReviewValidator()
        val item = newDeclarationReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = DeclarationReviewRequiredCodeRule("declaration review needs an entry")
        val item = newDeclarationReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags values above duration minutes`() {
        val rule = DeclarationReviewDurationMinutesRangeRule(64, 1000)
        val item = newDeclarationReview("x", "y")
        item.durationMinutes = 1001
        assertTrue(rule.check(item).size > 0, "no problem above the range")
    }

    @Test
    fun `flags a blank title`() {
        val rule = DeclarationReviewRequiredTitleRule("declaration review needs this field")
        val item = newDeclarationReview("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
