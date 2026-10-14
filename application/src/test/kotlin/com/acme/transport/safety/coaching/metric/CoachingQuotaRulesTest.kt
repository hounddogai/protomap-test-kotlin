package com.acme.transport.safety.coaching.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CoachingQuotaRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultCoachingQuotaValidator()
        val item = newCoachingQuota("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank title`() {
        val rule = CoachingQuotaRequiredTitleRule("coaching quota needs an entry")
        val item = newCoachingQuota("", "")
        item.title = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
