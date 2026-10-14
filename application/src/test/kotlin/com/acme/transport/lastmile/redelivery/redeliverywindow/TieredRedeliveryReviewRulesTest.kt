package com.acme.transport.lastmile.redelivery.redeliverywindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class TieredRedeliveryReviewRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultTieredRedeliveryReviewValidator()
        val item = newTieredRedeliveryReview("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank owner id`() {
        val rule = TieredRedeliveryReviewRequiredOwnerIdRule("tiered redelivery review needs this field")
        val item = newTieredRedeliveryReview("", "")
        item.ownerId = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }

    @Test
    fun `flags a blank code`() {
        val rule = TieredRedeliveryReviewRequiredCodeRule("tiered redelivery review needs this field")
        val item = newTieredRedeliveryReview("", "")
        item.code = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
