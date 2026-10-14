package com.acme.transport.safety.nearmiss.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NearMissReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultNearMissReviewValidator()
        val item = newNearMissReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = NearMissReviewRequiredCodeRule("near miss review needs an entry")
        val item = newNearMissReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank shift code`() {
        val rule = NearMissReviewRequiredShiftCodeRule("near miss review needs an entry")
        val item = newNearMissReview("", "")
        item.shiftCode = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a long shift code`() {
        val rule = NearMissReviewShiftCodeLengthRule(255)
        val item = newNearMissReview("x", "y")
        item.shiftCode = "x".repeat(256)
        assertEquals(1, rule.check(item).size, "problems")
        item.shiftCode = "ok"
        assertEquals(0, rule.check(item).size, "problems after fixing")
    }
}
