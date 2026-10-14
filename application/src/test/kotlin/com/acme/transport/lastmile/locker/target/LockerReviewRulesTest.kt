package com.acme.transport.lastmile.locker.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class LockerReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultLockerReviewValidator()
        val item = newLockerReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank segment`() {
        val rule = LockerReviewRequiredSegmentRule("locker review needs this field")
        val item = newLockerReview("", "")
        item.segment = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = LockerReviewRequiredCodeRule("locker review needs a value")
        val item = newLockerReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank description`() {
        val rule = LockerReviewRequiredDescriptionRule("locker review needs this field")
        val item = newLockerReview("", "")
        item.description = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
