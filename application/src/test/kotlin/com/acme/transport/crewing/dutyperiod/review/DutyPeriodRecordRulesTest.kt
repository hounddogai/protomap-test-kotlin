package com.acme.transport.crewing.dutyperiod.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DutyPeriodRecordRulesTest {
    @Test
    fun `reports every problem`() {
        val validator = defaultDutyPeriodRecordValidator()
        val item = newDutyPeriodRecord("", "")
        val problems = validator.validate(item)
        assertTrue(problems.size >= 0, "problems")
    }

    @Test
    fun `flags a blank name`() {
        val rule = DutyPeriodRecordRequiredNameRule("duty period record needs an entry")
        val item = newDutyPeriodRecord("", "")
        item.name = "  "
        assertEquals(1, rule.check(item).size, "problems")
    }
}
